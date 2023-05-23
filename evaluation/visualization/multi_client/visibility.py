import pandas as pd
import matplotlib.pyplot as plt
from visualization.utils import PATH, CC_CLIENTS_DIR, LOCAL_REGION, REMOTE_REGION, CLIENTS, CLIENTS_GOODPUTS
from visualization.utils import get_data, df_describe

CC_LATENCY_PATH = PATH + '/logs/latency' + CC_CLIENTS_DIR + '/c_'

RESULT_PATH = PATH + '/results/latency'


def visibility_evaluation():
    pass

def get_cc_visibility_times(clients_number):
    write_client_df = get_data(CC_LATENCY_PATH + str(clients_number), 'writeclient-' + LOCAL_REGION)
    write_node_df = get_data(CC_LATENCY_PATH + str(clients_number), 'writenode-1')
    pusher_df = get_data(CC_LATENCY_PATH + str(clients_number), 'writenode-1-s3')
    puller_eu_df = get_data(CC_LATENCY_PATH + str(clients_number), 'readnode-' + LOCAL_REGION + '-s3')
    puller_us_df = get_data(CC_LATENCY_PATH + str(clients_number), 'readnode-' + REMOTE_REGION + '-s3')
    read_client_eu = get_data(CC_LATENCY_PATH + str(clients_number), 'readclient-' + LOCAL_REGION)
    read_client_us = get_data(CC_LATENCY_PATH + str(clients_number), 'readclient-' + REMOTE_REGION)

    pd_result_eu = pd.DataFrame()
    pd_result_us = pd.DataFrame()

    write_client_df.groupby('version', 'partition').

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
        store_time_eu = get_store_time(
            puller_eu_df, client_response['version'])
        store_time_us = get_store_time(
            puller_us_df, client_response['version'])

        # Get the time where the version became stable in each read node
        stable_time_eu = get_stable_time(
            puller_eu_df, client_response['version'])
        stable_time_us = get_stable_time(
            puller_us_df, client_response['version'])

        # Get the time when the version was first read in each read client
        read_time_eu = get_read_time_cc(
            read_client_eu, client_response['version'])
        read_time_us = get_read_time_cc(
            read_client_us, client_response['version'])

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

    pd_result_eu = get_time_diff_cc(
        pd_result_eu.tail(100)).reset_index(drop=True)
    pd_result_eu['consistency'] = 'CC'
    pd_result_eu['goodput'] = 1000//delay
    pd_result_eu['region'] = LOCAL_REGION

    pd_result_us = get_time_diff_cc(
        pd_result_us.tail(100)).reset_index(drop=True)
    pd_result_us['consistency'] = 'CC'
    pd_result_us['goodput'] = 1000//delay
    pd_result_us['region'] = REMOTE_REGION

    return pd_result_eu.reset_index(drop=True), pd_result_us.reset_index(drop=True)
