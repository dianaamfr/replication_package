import os
import re
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from utils import PATH, LOCAL_REGION, REMOTE_REGION, MARKERS, PALETTE_FULL
from utils import get_data, get_diff, df_describe
import numpy as np
import math

RAW_PATH = PATH + '/logs/multi_client/read_times/partitions'
RESULT_PATH = PATH + '/results/multi_client/visibility'
MAX_REQUESTS = 500
pattern = r"r(\d+)_w(\d+)"

def visibility_evaluation():
    visibility_with_clients()
    visibility_with_partitions('r5_w10')

def visibility_with_clients():
    dir = os.path.join(RAW_PATH, 'p1')
    test_names = [name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]
    dfs = []

    for test_name in test_names:
        file_path = os.path.join(dir, test_name)
        df_eu, df_us = get_visibility_times(file_path)
        df_stats_eu = df_describe(df_eu, 'read_time').round(2)
        df_stats_us = df_describe(df_us, 'read_time').round(2)

        df_stats_eu['read_throughput'] = get_read_throughput(file_path)
        df_stats_us['read_throughput'] = get_read_throughput(file_path)
        
        (r, w) = re.findall(pattern, test_name)[0]
        df_stats_eu['read_clients'] = int(r)
        df_stats_us['read_clients'] = int(r)
        df_stats_us['write_clients'] = int(w)
        df_stats_eu['write_clients'] = int(w)

        df_stats_eu['rw'] = r + ':' + w
        df_stats_us['rw'] = r + ':' + w

        df_stats_eu['region'] = LOCAL_REGION
        df_stats_us['region'] = REMOTE_REGION

        dfs.append(df_stats_eu)
        dfs.append(df_stats_us)

    df_result = pd.concat(dfs, ignore_index=True).sort_values(by=['read_clients', 'write_clients']).reset_index(drop=True)

    g = sns.FacetGrid(df_result, col="region", height=4, aspect=2, margin_titles=True)
    g.map(sns.barplot, "rw", "mean", width=0.6, linewidth=1, edgecolor='black', alpha=0.9, errorbar=None)
    g.add_legend()  
    g.set(yscale="log", yticks=[math.pow(10, i) for i in range(3, 6)])
    g.set_axis_labels("Readers : Writers (ms)", "Average Staleness (ms)")
    g.axes[0][0].set_title("Local Region (EU)", pad=5)
    g.axes[0][1].set_title("Remote Region (US)", pad=5)

    plt.savefig(RESULT_PATH + '/visibility_with_clients.png', dpi=300)
    plt.clf()
    plt.close()


def visibility_with_partitions(test_name):
    dfs = []
    partition_dirs = [name for name in os.listdir(RAW_PATH) if os.path.isdir(os.path.join(RAW_PATH, name))]
    for partition_dir in partition_dirs:
        partitions = int(re.findall(r"p(\d+)", partition_dir)[0])
        file_path = os.path.join(RAW_PATH, partition_dir , test_name)
        df_eu, df_us = get_visibility_times(file_path)
        df_eu['partitions'] = partitions
        df_us['partitions'] = partitions
        dfs.append(df_eu)
        dfs.append(df_us)

    df_result = pd.concat(dfs, ignore_index=True).reset_index(drop=True)

    # Visibility
    g = sns.FacetGrid(df_result, col="region", height=5, aspect=0.8, margin_titles=True)
    g.map(sns.barplot, "partitions", "read_time", width=0.6, linewidth=1, edgecolor='black', alpha=0.9)
    g.add_legend()  
    g.set(yscale="log", yticks=[math.pow(10, i) for i in range(3, 6)])
    g.set_axis_labels("Inter-Write Delay (ms)", "Visibility (ms)")
    g.axes[0][0].set_title("Local Region (EU)", pad=5)
    g.axes[0][1].set_title("Remote Region (US)", pad=5)

    plt.savefig(RESULT_PATH + '/visibility_with_partitions.png', dpi=300)
    plt.clf()
    plt.close()

    # Response Time
    g = sns.FacetGrid(df_result, col="region", height=5, aspect=0.8, margin_titles=True)
    g.map(sns.barplot, "partitions", "response_time", width=0.6, linewidth=1, edgecolor='black', alpha=0.9)
    g.add_legend()  
    g.set(yscale="log", yticks=[1, 10, 100])
    g.set_axis_labels("Inter-Write Delay (ms)", "Write Latency (ms)")
    g.axes[0][0].set_title("Local Region (EU)", pad=5)
    g.axes[0][1].set_title("Remote Region (US)", pad=5)

    plt.savefig(RESULT_PATH + '/write_response_with_partitions.png', dpi=300)
    plt.clf()
    plt.close()


def get_visibility_times(path):
    pushers = [name.split('.')[0] for name in os.listdir(path) if re.match(r'writenode-\d-s3', name)]
    write_nodes = [name.split('.')[0] for name in os.listdir(path) if re.match(r'writenode-\d.json', name)]
    write_clients = [name.split('.')[0] for name in os.listdir(path) if re.match(r'writeclient', name)]

    pusher_dfs = [(int(re.findall(r'writenode-(\d+)-s3', name)[0]), get_data(path, name)) for name in pushers]
    write_node_dfs = [(int(re.findall(r'writenode-(\d+)', name)[0]), get_data(path, name)) for name in write_nodes]
    
    write_client_df = pd.DataFrame()
    if len(write_clients) != 1:
        write_client_dfs = [get_data(path, name) for name in write_clients]
        write_client_df = pd.concat(write_client_dfs, ignore_index=True)
    else:
        write_client_df = get_data(path, write_clients[0])

    puller_eu_df = get_data(path, 'readnode-' + LOCAL_REGION + '-s3')
    puller_us_df = get_data(path, 'readnode-' + REMOTE_REGION + '-s3')
    read_client_eu = get_data(path, 'readclient-' + LOCAL_REGION)
    read_client_us = get_data(path, 'readclient-' + REMOTE_REGION)

    pd_result_eu = pd.DataFrame()
    pd_result_us = pd.DataFrame()

    i = 0
    write_requests = write_client_df.sort_values(by=['version', 'partition']).groupby(['version', 'partition'])
    for group_key in write_requests.groups:
        if i == MAX_REQUESTS:
            break
        group = write_requests.get_group(group_key)
        if len(group) != 2:
            continue

        client_request = group.iloc[0]
        client_response = group.iloc[1]
        partition = client_request['partition']
        version = client_request['version']
        push_node_df = [pair for pair in pusher_dfs if pair[0] == partition][0][1]
        write_node_df = [pair for pair in write_node_dfs if pair[0] == partition][0][1]

        # Get request time
        request_time = client_request['time']

        # Get response time
        response_time = client_response['time']

        read_time_eu = None
        read_time_us = None
        try:
            # Get the time when the version was first available for read clients
            read_time_eu = get_read_time(read_client_eu, version)
            read_time_us = get_read_time(read_client_us, version)
        except:
            continue

        # Get the time when the write node received the request
        write_node_receive = get_write_receive_time(write_node_df, version)

        # Get the time when the write node responded to the request
        write_node_respond = get_write_response_time(write_node_df, version)

        # Get the time when the version was first pushed in the write node
        push_time = get_push_time(push_node_df, version)

        # Get the time when the version was first pulled in each read node
        pull_time_eu = get_pull_time(puller_eu_df, version)
        pull_time_us = get_pull_time(puller_us_df, version)

        # Get the time where the version became stable in each read node
        stable_time_eu = get_stable_time(puller_eu_df, version)
        stable_time_us = get_stable_time(puller_us_df, version)

        pd_result_eu = pd_result_eu.append({
            'version': version,
            'client_request': request_time,
            'write_node_receive': write_node_receive,
            'write_node_respond': write_node_respond,
            'client_response': response_time,
            'write_node_push': push_time,
            'read_node_pull': pull_time_eu,
            'read_node_stable': stable_time_eu,
            'read_client_response': read_time_eu,
        }, ignore_index=True)

        pd_result_us = pd_result_us.append({
            'version': client_response['version'],
            'client_request': request_time,
            'write_node_receive': write_node_receive,
            'write_node_respond': write_node_respond,
            'client_response': response_time,
            'write_node_push': push_time,
            'read_node_pull': pull_time_us,
            'read_node_stable': stable_time_us,
            'read_client_response': read_time_us,
        }, ignore_index=True)

        i+=1

    pd_result_eu = get_time_diff(
        pd_result_eu).reset_index(drop=True)
    pd_result_eu['region'] = LOCAL_REGION

    pd_result_us = get_time_diff(
        pd_result_us).reset_index(drop=True)
    pd_result_us['region'] = REMOTE_REGION

    return pd_result_eu.reset_index(drop=True), pd_result_us.reset_index(drop=True)


def get_write_receive_time(df, version):
    return df[(df['logType'] == 'WRITE_REQUEST') & (df['version'] == version)].iloc[0]['time']

def get_write_response_time(df, version):
    return df[(df['logType'] == 'WRITE_RESPONSE') & (df['version'] == version)].iloc[0]['time']

def get_push_time(df, version):
    return df[(df['logType'] == 'LOG_PUSH') & (df['version'] >= version)].sort_values('version').iloc[0]['time']


def get_pull_time(df, version):
    return df[(df['logType'] == 'LOG_PULL') & (df['version'] >= version)].sort_values('version').iloc[0]['time']


def get_stable_time(df, version):
    return df[(df['logType'] == 'STABLE_TIME') & (df['stableTime'] >= version)].sort_values('stableTime').iloc[0]['time']


def get_read_time(df, version):
    return df[(df['logType'] == 'ROT_RESPONSE') & (df['stableTime'] >= version)].sort_values(['stableTime', 'time', 'id']).iloc[0]['time']


def get_time_diff(df):
    df_diff = pd.DataFrame()
    df_diff['response_time'] = get_diff(
        df, 'client_request', 'client_response')
    df_diff['push_time'] = get_diff(
        df, 'client_request', 'write_node_push')
    df_diff['pull_time'] = get_diff(
        df, 'client_request', 'read_node_pull')
    df_diff['stable_time'] = get_diff(
        df, 'client_request', 'read_node_stable')
    df_diff['read_time'] = get_diff(
        df, 'client_request', 'read_client_response')
    return df_diff


def get_log_y_lim(df):
    max_y = np.max(df)
    order_of_magnitude = np.floor(np.log10(max_y))
    return 10 ** (order_of_magnitude + 1)

def get_read_throughput(path):
    df = get_data(path, 'readclient-' + LOCAL_REGION)
    return (df.loc[df['logType'] == 'THROUGHPUT', 'total'].values[0] / 60).round(2)
