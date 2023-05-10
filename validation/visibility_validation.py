import pandas as pd
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

    return pd_result_eu, pd_result_us

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

    return pd_result_eu, pd_result_us

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

def get_table(df, title, filename):
    plt.annotate(title, (0.5, 0.7), xycoords='axes fraction', ha='center', va='bottom', fontsize=10)
    plt.table(cellText=df.values.round(2), colLabels=df.columns, loc='center', cellLoc='center')
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
