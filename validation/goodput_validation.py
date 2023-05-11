import pandas as pd
import matplotlib.pyplot as plt
from utils import get_data
import math
import os

PAYLOAD_BYTES = 12
PATH = os.path.dirname(os.path.abspath(__file__))
LOGS_DIR = PATH + '/logs/goodput'

def goodput_times(iteration_dir, region):
    df = get_data(LOGS_DIR + '/' + iteration_dir, 'readclient-' + region)
    
    df_result = pd.DataFrame()
    df_result['elapsed_time'] = df['time']
    df_result['total_writes'] = df['totalVersions']
    df_result['total_bytes'] = (df_result['total_writes'] * PAYLOAD_BYTES)
    df_result['writes_per_second'] = ((1000 * df_result['total_writes']) / df_result['elapsed_time'])
    df_result['bytes_per_second'] = (df_result['total_bytes'] / df_result['elapsed_time'])

    return df_result

def goodput_stats(df_ev_goodput, df_cc_goodput):
    df_ev_stats = df_ev_goodput.mean().to_frame().transpose()
    df_cc_stats = df_cc_goodput.mean().to_frame().transpose()
    df_ev_stats.index = ['EV']
    df_cc_stats.index = ['CC']

    df_result = pd.concat([df_ev_stats.round(2), df_cc_stats.round(2)])
    df_result['name'] = df_result.index
    df_result = df_result.reset_index(drop=True)

    df_result = pd.concat([df_result['name'], df_result.drop('name', axis=1)], axis=1)

    plt.table(cellText=df_result.values, colLabels=df_result.columns, cellLoc='center', loc='center')
    plt.axis('off')
    plt.annotate('Goodput', (0.5, 0.7), xycoords='axes fraction', ha='center', va='bottom', fontsize=10)
    plt.savefig(PATH + '/results/plots/goodput_table.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.rcParams['figure.figsize'] = plt.rcParamsDefault['figure.figsize']

def goodput_latency_relation(path_ev, path_cc):
    df_ev_50 = goodput_times(path_ev + '01', 'eu-west-1')
    df_ev_100 = goodput_times(path_ev + '02', 'eu-west-1')
    df_ev_200 = goodput_times(path_ev + '03', 'eu-west-1')

    df_cc_50 = goodput_times(path_cc + '01', 'eu-west-1')
    df_cc_100 = goodput_times(path_cc + '02', 'eu-west-1')
    df_cc_200 = goodput_times(path_cc + '03', 'eu-west-1')

    latencies = [50, 100, 200]
    df_ev = throughput_latency_df([df_ev_50, df_ev_100, df_ev_200], latencies)
    df_cc = throughput_latency_df([df_cc_50, df_cc_100, df_cc_200] , latencies)
    
    plt.figure(figsize=(10, 15))
    plt.plot(df_ev['latency'], df_ev['mean'], markersize=8, marker='s', linestyle='-', color='teal', label='EV Average Goodput')
    plt.plot(df_cc['latency'], df_cc['mean'], markersize=8, marker='o', linestyle='-', color='olive', label='CC Average Goodput')
    plt.plot(df_ev['latency'], df_ev['p99'], markersize=5, marker='s', linestyle='--', color='teal', label='EV 99th Percentile Goodput')
    plt.plot(df_cc['latency'], df_cc['p99'], markersize=5, marker='o', linestyle='--', color='olive', label='CC 99th Percentile Goodput')
    plt.grid(True)
    plt.legend()

    plt.xlabel('Goodput (writes/s)')
    plt.ylabel('Goodput (writes/s)')

    plt.title('Goodput for different read latencies')
    plt.savefig(PATH + '/results/plots/goodput_with_latency.png', dpi=300)
    plt.clf()
    plt.rcParams['figure.figsize'] = plt.rcParamsDefault['figure.figsize']


def throughput_latency_df(dfs, latencies):
    df_result = pd.DataFrame()
    for i in range(len(dfs)):
        df_result = df_result.append({
            'latency': latencies[i], 
            'mean': dfs[i]['writes_per_second'].mean(), 
            'p99': dfs[i]['writes_per_second'].quantile(q=0.99)}, 
            ignore_index=True)
    return df_result
