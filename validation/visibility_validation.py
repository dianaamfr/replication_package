import csv
import pandas as pd
from utils import get_data, get_file, get_diff
import os

PATH = os.path.dirname(os.path.abspath(__file__))
LOGS_DIR = PATH + '/logs/visibility'
LOG_RESULT_FILE = PATH + '/results/visibility/logs.csv'
LOG_STATS_FILE = PATH + '/results/visibility/validation.csv'

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

def get_read_time(df, version):
    return df[(df['logType'] == 'ROT_RESPONSE') & (df['stableTime'] >= version)].sort_values('stableTime').iloc[0]['time']


def combine_logs():
    dest_file = open(LOG_RESULT_FILE ,'w+', newline='\n', encoding='utf-8')
    writer = csv.writer(dest_file)
    writer.writerow(get_header())

    # Log files
    write_client = get_file(LOGS_DIR, 'writeclient-eu-west-1')
    write_node = get_file(LOGS_DIR, 'writenode-1')
    
    # Data frames
    pusher_df = get_data(LOGS_DIR, 'writenode-1-s3')
    puller_eu_df = get_data(LOGS_DIR, 'readnode-eu-west-1-s3')
    puller_us_df = get_data(LOGS_DIR, 'readnode-us-east-1-s3')
    
    read_client_eu = get_data(LOGS_DIR, 'readclient-eu-west-1')
    read_client_us = get_data(LOGS_DIR, 'readclient-us-east-1')

    i = 0
    for client_request, client_response in zip(write_client[::2], write_client[1::2]):
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
        read_time_eu = get_read_time(read_client_eu, client_response['version'])
        read_time_us = get_read_time(read_client_us, client_response['version'])


        writer.writerow([
            client_response['version'], 
            client_request['time'], 
            write_node[i]['time'],  
            write_node[i+1]['time'], 
            client_response['time'],
            push_time, 
            pull_time_eu,
            store_time_eu,
            stable_time_eu,
            pull_time_us,
            store_time_us,
            stable_time_us,
            read_time_eu,
            read_time_us
            ]
            )
        i+=2

def get_stats():
    df = pd.read_csv(LOG_RESULT_FILE)
    #df = df.drop(index=0)
    dest_file = open(LOG_STATS_FILE ,'w+', newline='\n', encoding='utf-8')
    writer = csv.writer(dest_file)
    writer.writerow(['', 
        'Write to response', 
        'Write to push',
        'Write to pull (EU)',
        'Write to pull (US)',
        'Write to stable (EU)', 
        'Write to stable (US)', 
        'Write to store (EU)', 
        'Write to store (US)',
        'Write to read (EU)',
        'Write to read (US)'
        ])

    response_time = get_diff(df, 'client_request', 'client_response')
    push_time = get_diff(df, 'client_request', 'write_node_push')
    pull_time_eu = get_diff(df, 'client_request', 'read_node_eu_pull')
    pull_time_us = get_diff(df, 'client_request', 'read_node_us_pull')
    store_time_eu = get_diff(df, 'client_request', 'read_node_eu_store')
    store_time_us = get_diff(df, 'client_request', 'read_node_us_store')
    stable_time_eu = get_diff(df, 'client_request', 'read_node_eu_stable')
    stable_time_us = get_diff(df, 'client_request', 'read_node_us_stable')
    read_time_eu = get_diff(df, 'client_request', 'read_client_eu_response')
    read_time_us = get_diff(df, 'client_request', 'read_client_us_response')

    data = {'response_time': response_time,
        'push_time': push_time,
        'pull_time_eu': pull_time_eu,
        'pull_time_us': pull_time_us,
        'store_time_eu': store_time_eu,
        'store_time_us': store_time_us,
        'stable_time_eu': stable_time_eu,
        'stable_time_us': stable_time_us,
        'read_time_eu': read_time_eu,
        'read_time_us': read_time_us}

    df_results = pd.DataFrame(data)

    writer.writerow(['99%'] + df_results.quantile(q=0.99).values.tolist())
    writer.writerow(['95%'] + df_results.quantile(q=0.95).values.tolist())
    writer.writerow(['50%'] + df_results.quantile(q=0.50).values.tolist())
    writer.writerow(['mean'] + df_results.mean().values.tolist())
    writer.writerow(['max'] + df_results.max().values.tolist())
    writer.writerow(['min'] + df_results.min().values.tolist())

if __name__ == '__main__':
    combine_logs()
    get_stats()