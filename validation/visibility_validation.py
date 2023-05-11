import pandas as pd
import seaborn as sns
import math
import matplotlib.pyplot as plt
from utils import get_data, get_diff
import os

PATH = os.path.dirname(os.path.abspath(__file__))
LOGS_DIR = PATH + '/logs/visibility'

def get_header():
    return [
        'version', 
        'client_request', 
        'write_node_receive', 
        'write_node_respond', 
        'client_response', 
        'write_node_push', 
        'read_node_eu_pull',
        'read_node_eu_store', 
        'read_node_eu_stable',
        'read_node_us_pull',
        'read_node_us_store',
        'read_node_us_stable', 
        'read_client_eu_response', 
        'read_client_us_response']

def get_push_time(df, version):
    return df[(df['logType'] == 'LOG_PUSH') & (df['version'] >= version)].sort_values('version').iloc[0]['time']

def get_pull_time(df, version):
    return df[(df['logType'] == 'LOG_PULL') & (df['version'] >= version)].sort_values('version').iloc[0]['time']

def get_store_time(df, version):
    return df[(df['logType'] == 'STORE_VERSION') & (df['version'] == version)].iloc[0]['time']

def get_stable_time(df, version):
    return df[(df['logType'] == 'STABLE_TIME') & (df['stableTime'] >= version)].sort_values('stableTime').iloc[0]['time']

def get_read_time_cc(df, version):
    return df[(df['logType'] == 'ROT_RESPONSE') & (df['stableTime'] >= version)].sort_values('stableTime').iloc[0]['time']

def get_read_time_ev(df, id):
    return df[(df['logType'] == 'ROT_RESPONSE') & (df['id'] >= id)].sort_values('id').iloc[0]['time']

def ev_visibility_times(iteration_dir):
    write_client_df = get_data(LOGS_DIR + '/' + iteration_dir, 'writeclient-eu-west-1')
    read_client_eu = get_data(LOGS_DIR + '/' + iteration_dir, 'readclient-eu-west-1')
    read_client_us = get_data(LOGS_DIR + '/' + iteration_dir, 'readclient-us-east-1')

    pd_result_eu = pd.DataFrame();
    pd_result_us = pd.DataFrame();

    for i in range(0, len(write_client_df), 2):
        client_request = write_client_df.iloc[i]
        client_response = write_client_df.iloc[i+1]

        # Get request time
        request_time = client_request['time']

        # Get response time
        response_time = client_response['time']

        # Get the time when the version was first read in each read client
        read_time_eu = get_read_time_ev(read_client_eu, client_response['id'])
        read_time_us = get_read_time_ev(read_client_us, client_response['id'])

        pd_result_eu = pd_result_eu.append({
            'version': client_response['id'],
            'client_request': request_time,
            'client_response': response_time,
            'read_client_response': read_time_eu,
            }, ignore_index=True)

        pd_result_us = pd_result_us.append({
            'version': client_response['id'],
            'client_request': request_time,
            'client_response': response_time,
            'read_client_response': read_time_us,
            }, ignore_index=True)

    return pd_result_eu.tail(100), pd_result_us.tail(100)

def cc_visibility_times(iteration_dir):
    write_client_df = get_data(LOGS_DIR + '/' + iteration_dir, 'writeclient-eu-west-1')
    write_node_df = get_data(LOGS_DIR + '/' + iteration_dir, 'writenode-1')
    pusher_df = get_data(LOGS_DIR + '/' + iteration_dir, 'writenode-1-s3')
    puller_eu_df = get_data(LOGS_DIR + '/' + iteration_dir, 'readnode-eu-west-1-s3')
    puller_us_df = get_data(LOGS_DIR + '/' + iteration_dir, 'readnode-us-east-1-s3')
    read_client_eu = get_data(LOGS_DIR + '/' + iteration_dir, 'readclient-eu-west-1')
    read_client_us = get_data(LOGS_DIR + '/' + iteration_dir, 'readclient-us-east-1')

    pd_result_eu = pd.DataFrame();
    pd_result_us = pd.DataFrame();
   
    for i in range(0, len(write_client_df), 2):
        client_request = write_client_df.iloc[i]
        client_response = write_client_df.iloc[i+1]

        # Get request time
        request_time = client_request['time']

        # Get response time
        response_time = client_response['time']

        # Get the time when the version was first pushed in the write node
        push_time = get_push_time(pusher_df, client_response['version'])

        # Get the time when the version was first pulled in each read node
        pull_time_eu = get_pull_time(puller_eu_df, client_response['version'])
        pull_time_us = get_pull_time(puller_us_df, client_response['version'])

        # Get the time when the version was stored in each read node
        store_time_eu = get_store_time(puller_eu_df, client_response['version'])
        store_time_us = get_store_time(puller_us_df, client_response['version'])

        # Get the time where the version became stable in each read node
        stable_time_eu = get_stable_time(puller_eu_df, client_response['version'])
        stable_time_us = get_stable_time(puller_us_df, client_response['version'])

        # Get the time when the version was first read in each read client
        read_time_eu = get_read_time_cc(read_client_eu, client_response['version'])
        read_time_us = get_read_time_cc(read_client_us, client_response['version'])

        pd_result_eu = pd_result_eu.append({
            'version': client_response['version'],
            'client_request': request_time,
            'write_node_receive': write_node_df.iloc[i]['time'],
            'write_node_respond': write_node_df.iloc[i+1]['time'],
            'client_response': response_time,
            'write_node_push': push_time,
            'read_node_pull': pull_time_eu,
            'read_node_store': store_time_eu,
            'read_node_stable': stable_time_eu,
            'read_client_response': read_time_eu,
            }, ignore_index=True)

        pd_result_us = pd_result_us.append({
            'version': client_response['version'],
            'client_request': request_time,
            'write_node_receive': write_node_df.iloc[i]['time'],
            'write_node_respond': write_node_df.iloc[i+1]['time'],
            'client_response': response_time,
            'write_node_push': push_time,
            'read_node_pull': pull_time_us,
            'read_node_store': store_time_us,
            'read_node_stable': stable_time_us,
            'read_client_response': read_time_us,
            }, ignore_index=True)

    return pd_result_eu.tail(100), pd_result_us.tail(100)

def get_table(df, title, filename):
    df = df.round(2)
    df['name'] = df.index
    columns = list(df.columns)
    columns.remove('name')
    new_order = ['name'] + columns
    df = df[new_order]

    df.reset_index(drop=True, inplace=True)
    plt.annotate(title, (0.5, 0.7), xycoords='axes fraction', ha='center', va='bottom', fontsize=10)
    plt.table(cellText=df.values, colLabels=df.columns, loc='center', cellLoc='center')
    plt.axis('off')
    plt.savefig(PATH + '/results/plots/' + filename + '.png', dpi=300, bbox_inches='tight')
    plt.clf()

def get_time_diff_ev(df_ev):
    df_ev_diff = pd.DataFrame()
    df_ev_diff['response_time'] = get_diff(df_ev, 'client_request', 'client_response')
    df_ev_diff['read_time'] = get_diff(df_ev, 'client_request', 'read_client_response')
    return df_ev_diff

def get_time_diff_cc(df_cc):
    df_cc_diff = pd.DataFrame()
    df_cc_diff['response_time'] = get_diff(df_cc, 'client_request', 'client_response')
    df_cc_diff['push_time'] = get_diff(df_cc, 'client_request', 'write_node_push')
    df_cc_diff['pull_time'] = get_diff(df_cc, 'client_request', 'read_node_pull')
    df_cc_diff['store_time'] = get_diff(df_cc, 'client_request', 'read_node_store')
    df_cc_diff['stable_time'] = get_diff(df_cc, 'client_request', 'read_node_stable')
    df_cc_diff['read_time'] = get_diff(df_cc, 'client_request', 'read_client_response')
    return df_cc_diff

def get_stats(df):
    return pd.DataFrame({
        '99%': df.quantile(0.99),
        '95%': df.quantile(0.95),
        '70%': df.quantile(0.7),
        '50%': df.quantile(0.5),
        'mean': df.mean(),
        'min': df.min(),
        'max': df.max()})

def visibility_tables(df_ev_eu, df_cc_eu, df_ev_us, df_cc_us):
    # Write Latency & Visibility comparison table
    df_ev_eu_stats = get_stats(df_ev_eu)
    df_cc_eu_stats = get_stats(df_cc_eu)
    df_ev_us_stats = get_stats(df_ev_us)
    df_cc_us_stats = get_stats(df_cc_us)

    get_table(df_ev_eu_stats, 'Eventual Consistency Write Latency & Visibility - EU', 'write_latency_visibility_ev_eu.png')
    get_table(df_cc_eu_stats, 'Causal Consistency Write Latency & Visibility - EU', 'write_latency_visibility_cc_eu.png')
    get_table(df_ev_us_stats, 'Eventual Consistency Write Latency & Visibility - US', 'write_latency_visibility_ev_us.png')
    get_table(df_cc_us_stats, 'Causal Consistency Write Latency & Visibility - US', 'write_latency_visibility_cc_us.png')

def addPercentiles(df, percentiles, ax):
    visibility_ev_eu_summary = df['read_time'].describe(percentiles=[p/100 for p in percentiles])

    for p in percentiles:
        ax.axhline(visibility_ev_eu_summary[f'{p}%'], linestyle='--', color='green', label=f'{p}%')

    ax.axhline(visibility_ev_eu_summary['mean'], linestyle='-', color='blue', label='mean')
    ax.axhline(visibility_ev_eu_summary['min'], linestyle=':', color='gray', label='min')
    ax.axhline(visibility_ev_eu_summary['max'], linestyle=':', color='gray', label='max')
    ax.legend()
    ax.set_xlabel('')

def visibility_boxplot(df_ev_eu, df_cc_eu, df_ev_us, df_cc_us):
    percentiles = [50, 70, 95, 99]
    fig, axs = plt.subplots(nrows=2, ncols=2, figsize=(10, 15))

    min_visibility_eu = math.ceil(min(min(df_ev_eu['read_time']), min(df_cc_eu['read_time'])))
    min_visibility_us = math.ceil(min(min(df_ev_us['read_time']), min(df_cc_us['read_time'])))

    max_visibility_eu = math.ceil(max(max(df_ev_eu['read_time']), max(df_cc_eu['read_time'])))
    max_visibility_us = math.ceil(max(max(df_ev_us['read_time']), max(df_cc_us['read_time'])))

    axs[0,0].set_ylim(min_visibility_eu, max_visibility_eu)
    axs[0,1].set_ylim(min_visibility_eu, max_visibility_eu)
    axs[1,0].set_ylim(min_visibility_us, max_visibility_us)
    axs[1,1].set_ylim(min_visibility_us, max_visibility_us)

    df_ev_eu.plot.box(y='read_time', ax=axs[0,0], grid=True)
    axs[0,0].set_title('Eventually Consistent EU')
    
    df_cc_eu.plot.box(y='read_time', ax=axs[0,1], grid=True)
    axs[0,1].set_title('Causally Consistent EU')
    
    df_ev_us.plot.box(y='read_time', ax=axs[1,0], grid=True)
    axs[1,0].set_title('Eventually Consistent US')

    df_cc_us.plot.box(y='read_time', ax=axs[1,1], grid=True)
    axs[1,1].set_title('Causally Consistent US')

    addPercentiles(df_ev_eu, percentiles, axs[0,0])
    addPercentiles(df_cc_eu, percentiles, axs[0,1])
    addPercentiles(df_ev_us, percentiles, axs[1,0])
    addPercentiles(df_cc_us, percentiles, axs[1,1])

    fig.suptitle('Visibility Distribution')

    plt.tight_layout()    
    plt.savefig(PATH + '/results/plots/visibility_boxplot.png', dpi=300)
    plt.clf()
    plt.rcParams['figure.figsize'] = plt.rcParamsDefault['figure.figsize']

def stable_time_boxplot(df_eu, df_us):
    percentiles = [50, 70, 95, 99]
    fig, axs = plt.subplots(nrows=1, ncols=2, figsize=(10, 8))

    min_stable_time = math.ceil(min(min(df_eu['read_time']), min(df_us['read_time'])))
    max_stable_time = math.ceil(max(max(df_eu['read_time']), max(df_us['read_time'])))

    axs[0].set_ylim(min_stable_time, max_stable_time)
    axs[1].set_ylim(min_stable_time, max_stable_time)

    df_eu.plot.box(y='stable_time', ax=axs[0], grid=True)
    axs[0].set_title('EU')

    df_us.plot.box(y='stable_time', ax=axs[1], grid=True)
    axs[1].set_title('US')

    addPercentiles(df_eu, percentiles, axs[0])
    addPercentiles(df_us, percentiles, axs[1])

    plt.savefig(PATH + '/results/plots/stable_time_boxplot.png', dpi=300)
    plt.clf()
    plt.rcParams['figure.figsize'] = plt.rcParamsDefault['figure.figsize']

def visibility_histogram(df_ev_eu, df_cc_eu, df_ev_us, df_cc_us):
    fig, axs = plt.subplots(2, 2, figsize=(10, 15))

    sns.histplot(data=df_ev_eu, x="read_time", kde=True, color="skyblue", ax=axs[0, 0])
    axs[0,0].set_title('Eventually Consistent EU')
    axs[0,0].set_xlabel('Read Time')
    
    sns.histplot(data=df_ev_us, x="read_time", kde=True, color="olive", ax=axs[0, 1])
    axs[0,1].set_title('Eventually Consistent US')
    axs[0,1].set_xlabel('Read Time')
    
    sns.histplot(data=df_cc_eu, x="read_time", kde=True, color="gold", ax=axs[1, 0])
    axs[1,0].set_title('Causally Consistent EU')
    axs[1,0].set_xlabel('Read Time')

    sns.histplot(data=df_cc_us, x="read_time", kde=True, color="teal", ax=axs[1, 1])
    axs[1,1].set_title('Causally Consistent US')
    axs[1,1].set_xlabel('Read Time')

    plt.suptitle('Visibility Distribution')
    plt.savefig(PATH + '/results/plots/visibility_histogram.png', dpi=300)
    plt.clf()
    plt.rcParams['figure.figsize'] = plt.rcParamsDefault['figure.figsize']

def visibility_histogram_cc(df_cc_eu, df_cc_us):
    fig, axs = plt.subplots(3, 2, figsize=(20, 10))

    sns.histplot(data=df_cc_eu, x="response_time", kde=True, color="skyblue", ax=axs[0, 0])
    axs[0,0].set_xlabel('Response Time')

    sns.histplot(data=df_cc_eu, x="push_time", kde=True, color="olive", ax=axs[0,1])
    axs[0,1].set_xlabel('Push Time')

    sns.histplot(data=df_cc_eu, x="pull_time", kde=True, color="gold", ax=axs[1, 0])
    axs[1,0].set_xlabel('Push Time')

    sns.histplot(data=df_cc_eu, x="store_time", kde=True, color="teal", ax=axs[1, 1])
    axs[1,1].set_xlabel('Store Time')

    sns.histplot(data=df_cc_eu, x="stable_time", kde=True, color="magenta", ax=axs[2, 0])
    axs[2,0].set_xlabel('Stable Time')

    sns.histplot(data=df_cc_us, x="read_time", kde=True, color="red", ax=axs[2, 1])
    axs[2,1].set_xlabel('Read Time')

    plt.suptitle('Causally Consistent Write & Latency Visibility')
    plt.savefig(PATH + '/results/plots/visibility_cc_histogram.png', dpi=300)
    plt.clf()
    plt.rcParams['figure.figsize'] = plt.rcParamsDefault['figure.figsize']

def visibility_throughput_relation(path_ev, path_cc):
    df_ev_eu_200, df_ev_us_200 = ev_visibility_times(path_ev + '03')
    df_cc_eu_200, df_cc_us_200 = cc_visibility_times(path_cc + '03')
    
    df_ev_eu_100, df_ev_us_100 = ev_visibility_times(path_ev + '02')
    df_cc_eu_100, df_cc_us_100 = cc_visibility_times(path_cc + '02')
    
    df_ev_eu_50, df_ev_us_50 = ev_visibility_times(path_ev + '01')
    df_cc_eu_50, df_cc_us_50 = cc_visibility_times(path_cc + '01')

    throughput = [5000/200, 5000/100, 5000/50]
    dfs_ev_eu = [df_ev_eu_200, df_ev_eu_100, df_ev_eu_50]
    dfs_ev_us = [df_ev_us_200, df_ev_us_100, df_ev_us_50]
    dfs_cc_eu = [df_cc_eu_200, df_cc_eu_100, df_cc_eu_50]
    dfs_cc_us = [df_cc_us_200, df_cc_us_100, df_cc_us_50]

    for i in range(0, len(throughput)):
        dfs_ev_eu[i] = get_time_diff_ev(dfs_ev_eu[i])
        dfs_ev_us[i] = get_time_diff_ev(dfs_ev_us[i])
        dfs_cc_eu[i] = get_time_diff_ev(dfs_cc_eu[i])
        dfs_cc_us[i] = get_time_diff_ev(dfs_cc_us[i])

    df_ev_eu = throughput_latency_df(dfs_ev_eu, throughput)
    df_ev_us = throughput_latency_df(dfs_ev_us, throughput)
    df_cc_eu = throughput_latency_df(dfs_cc_eu, throughput)
    df_cc_us = throughput_latency_df(dfs_cc_us, throughput)

    max_latency_eu = math.ceil(max(df_ev_eu['p99'].max(), df_cc_eu['p99'].max()))
    max_latency_us = math.ceil(max(df_ev_us['p99'].max(), df_cc_us['p99'].max()))
    
    fig, axs = plt.subplots(1, 2, figsize=(20, 12))

    axs[0].plot(df_ev_eu['throughput'], df_ev_eu['mean'], markersize=8, marker='s', linestyle='-', color='teal', label='EV Average Visibility')
    axs[0].plot(df_cc_eu['throughput'], df_cc_eu['mean'], markersize=8, marker='o', linestyle='-', color='olive', label='CC Average Visibility')
    axs[0].plot(df_ev_eu['throughput'], df_ev_eu['p99'], markersize=5, marker='s', linestyle='--', color='teal', label='EV 99th Percentile Visibility')
    axs[0].plot(df_cc_eu['throughput'], df_cc_eu['p99'], markersize=5, marker='o', linestyle='--', color='olive', label='CC 99th Percentile Visibility')
    axs[0].grid(True)
    axs[0].legend()
    axs[0].set_xlabel('Throughput (writes/s)')
    axs[0].set_ylabel('Visibility (ms)')
    axs[0].set_yticks(range(0, max_latency_eu + 1, 1000))
    axs[0].set_title('EU Visibility vs Throughput')

    axs[1].plot(df_ev_us['throughput'], df_ev_us['mean'], markersize=8, marker='s', linestyle='-', color='teal', label='EV Average Visibility')
    axs[1].plot(df_cc_us['throughput'], df_cc_us['mean'], markersize=8, marker='o', linestyle='-', color='olive', label='CC Average Visibility')
    axs[1].plot(df_ev_us['throughput'], df_ev_us['p99'], markersize=5, marker='s', linestyle='--', color='teal', label='EV 99th Percentile Visibility')
    axs[1].plot(df_cc_us['throughput'], df_cc_us['p99'], markersize=5, marker='o', linestyle='--', color='olive', label='CC 99th Percentile Visibility')
    axs[1].grid(True)
    axs[1].legend()
    axs[1].set_xlabel('Throughput (writes/s)')
    axs[1].set_ylabel('Visibility (ms)')
    axs[1].set_yticks(range(0, max_latency_us + 1, 1000))
    axs[1].set_title('US Visibility vs Throughput')

    plt.suptitle('Visibility for different throughput values')
    plt.savefig(PATH + '/results/plots/visibility_with_throughput.png', dpi=300)
    plt.clf()
    plt.rcParams['figure.figsize'] = plt.rcParamsDefault['figure.figsize']

def throughput_latency_df(dfs, throughput):  
    df_result = pd.DataFrame()
    for i in range(len(dfs)):
        df_result = df_result.append({
            'throughput': throughput[i], 
            'mean': dfs[i]['read_time'].mean(), 
            'p99': dfs[i]['read_time'].quantile(q=0.99)}, 
            ignore_index=True)
    return df_result
