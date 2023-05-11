import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
from utils import get_data
import os
import math 

PATH = os.path.dirname(os.path.abspath(__file__))
LOGS_DIR = PATH + '/logs/latency'

def latency_times(iteration_dir, region):
    df = get_data(LOGS_DIR + '/' + iteration_dir, 'readclient-' + region)
    
    df_result = df.groupby('id').apply(lambda group: 
        group.loc[group['logType'] == 'ROT_RESPONSE', 'time'].values[0] - 
        group.loc[group['logType'] == 'ROT_REQUEST', 'time'].values[0]).reset_index(name='latency')

    return df_result.tail(100).reset_index(drop=True)

def latency_boxplot(df_ev_latency, df_cc_latency):
    percentiles = [50, 70, 95, 99]
    latency_ev_summary = df_ev_latency['latency'].describe(percentiles=[p/100 for p in percentiles])
    latency_cc_summary = df_cc_latency['latency'].describe(percentiles=[p/100 for p in percentiles])

    # Latency distribution boxplot
    fig, axs = plt.subplots(nrows=1, ncols=2, figsize=(10, 7))

    max_latency = max(max(df_ev_latency['latency']), max(df_cc_latency['latency'])) + 5
    axs[0].set_yticks(range(0, max_latency, 5))
    axs[1].set_yticks(range(0, max_latency, 5))

    df_ev_latency.plot.box(y='latency', ax=axs[0], grid=True)
    axs[0].set_title('Eventually Consistent Baseline EU')

    for p in percentiles:
        axs[0].axhline(latency_ev_summary[f'{p}%'], linestyle='--', color='green', label=f'{p}%')
        axs[1].axhline(latency_cc_summary[f'{p}%'], linestyle='--', color='green', label=f'{p}%')

    axs[0].axhline(latency_ev_summary['mean'], linestyle='-', color='blue', label='mean')
    axs[0].axhline(latency_ev_summary['min'], linestyle=':', color='gray', label='min')
    axs[0].axhline(latency_ev_summary['max'], linestyle=':', color='gray', label='max')
    axs[0].legend()
    axs[0].set_ylabel('Latency (ms)')

    axs[1].axhline(latency_ev_summary['mean'], linestyle='-', color='blue', label='mean')
    axs[1].axhline(latency_ev_summary['min'], linestyle=':', color='gray', label='min')
    axs[1].axhline(latency_ev_summary['max'], linestyle=':', color='gray', label='max')
    axs[1].legend()
    axs[1].set_ylabel('Latency (ms)')

    df_cc_latency.plot.box(y='latency', ax=axs[1], grid=True)
    axs[1].set_title('Causally Consistent Prototype EU')

    fig.suptitle('Latency Distributions')

    plt.tight_layout()    
    plt.savefig(PATH + '/results/plots/latency_boxplot.png', dpi=300)
    plt.clf()
    plt.rcParams['figure.figsize'] = plt.rcParamsDefault['figure.figsize']

def latency_stats(df_ev_latency, df_cc_latency): 
    df_ev_stats = latency_table(df_ev_latency)
    df_cc_stats = latency_table(df_cc_latency)
    df_ev_stats.index = ['EV']
    df_cc_stats.index = ['CC']

    df_result = pd.concat([df_ev_stats.round(2), df_cc_stats.round(2)])
    df_result['name'] = df_result.index
    df_result = df_result.reset_index(drop=True)

    df_result = pd.concat([df_result['name'], df_result.drop('name', axis=1)], axis=1)

    plt.table(cellText=df_result.values, colLabels=df_result.columns, cellLoc='center', loc='center')
    plt.axis('off')
    plt.annotate('Latency (ms)', (0.5, 0.7), xycoords='axes fraction', ha='center', va='bottom', fontsize=10)
    plt.savefig(PATH + '/results/plots/latency_table.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.rcParams['figure.figsize'] = plt.rcParamsDefault['figure.figsize']

def latency_histogram(df_ev_latency, df_cc_latency):
    plt.figure(figsize=(10, 7))
    sns.histplot(data=df_ev_latency, x="latency", kde=True, color="teal", label='EV')
    sns.histplot(data=df_cc_latency, x="latency", kde=True, color="orange", label='CC')

    plt.legend(loc='upper right')
    plt.xlabel('Latency (ms)')
    plt.ylabel('Frequency')

    plt.savefig(PATH + '/results/plots/latency_histogram.png', dpi=300)
    plt.clf()
    plt.rcParams['figure.figsize'] = plt.rcParamsDefault['figure.figsize']

def latency_table(df):
    return pd.DataFrame({
        '99%': [df['latency'].quantile(q=0.99)],
        '95%': [df['latency'].quantile(q=0.95)],
        '70%': [df['latency'].quantile(q=0.70)],
        '50%': [df['latency'].quantile(q=0.50)],
        'mean': [df['latency'].mean()],
        'max': [df['latency'].max()],
        'min': [df['latency'].min()]})


# Multivariate
def latency_throughput_relation(path_ev, path_cc):
    df_ev_200 = latency_times(path_ev + '03', 'eu-west-1')
    df_ev_100 = latency_times(path_ev + '02', 'eu-west-1')
    df_ev_50 = latency_times(path_ev + '01', 'eu-west-1')

    df_cc_200 = latency_times(path_cc + '03', 'eu-west-1')
    df_cc_100 = latency_times(path_cc + '02', 'eu-west-1')
    df_cc_50 = latency_times(path_cc + '01', 'eu-west-1')

    throughput = [5000/200, 5000/100, 5000/50]
    df_ev = latency_throughput_df([df_ev_200, df_ev_100, df_ev_50], throughput)
    df_cc = latency_throughput_df([df_cc_200, df_cc_100, df_cc_50] , throughput)

    max_latency = math.ceil(max(df_ev['p99'].max(), df_cc['p99'].max()))
    
    plt.figure(figsize=(10, 15))
    plt.plot(df_ev['throughput'], df_ev['mean'], markersize=8, marker='s', linestyle='-', color='teal', label='EV Average Latency')
    plt.plot(df_cc['throughput'], df_cc['mean'], markersize=8, marker='o', linestyle='-', color='olive', label='CC Average Latency')
    plt.plot(df_ev['throughput'], df_ev['p99'], markersize=5, marker='s', linestyle='--', color='teal', label='EV 99th Percentile Latency')
    plt.plot(df_cc['throughput'], df_cc['p99'], markersize=5, marker='o', linestyle='--', color='olive', label='CC 99th Percentile Latency')
    plt.grid(True)
    plt.legend()
    plt.yticks(range(0, max_latency, 5))

    plt.xlabel('Throughput (writes/s)')
    plt.ylabel('Latency (ms)')

    plt.title('Latency for different throughput values')
    plt.savefig(PATH + '/results/plots/latency_with_throughput.png', dpi=300)
    plt.clf()
    plt.rcParams['figure.figsize'] = plt.rcParamsDefault['figure.figsize']

def latency_throughput_df(dfs, throughput):  
    df_result = pd.DataFrame()
    for i in range(len(dfs)):
        df_result = df_result.append({
            'throughput': throughput[i], 
            'mean': dfs[i]['latency'].mean(), 
            'p99': dfs[i]['latency'].quantile(q=0.99)}, 
            ignore_index=True)
    return df_result
