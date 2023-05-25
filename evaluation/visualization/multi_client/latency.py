import os
import re
import numpy as np
import pandas as pd
import seaborn as sns
import matplotlib.ticker as ticker
import matplotlib.pyplot as plt
from visualization.utils import PATH, CC_CLIENTS_DIR, LOCAL_REGION
from visualization.utils import get_data, df_describe

CC_LATENCY_PATH = PATH + '/logs/latency' + CC_CLIENTS_DIR

RESULT_PATH = PATH + '/results/latency'
pattern = r"r(\d+)_w(\d+)"

def latency_evaluation(dir):
    path = CC_LATENCY_PATH + '/' + dir
    test_names = [name for name in os.listdir(path) if os.path.isdir(os.path.join(path, name))]
    dfs = []
    read_throughput = []
    for test_name in test_names:
        df = get_latency_times(path + '/' + test_name)
        dfs.append(df)
        read_throughput.append(get_read_throughput(path + '/' + test_name))

    latency_distribution_table(test_names, dfs, read_throughput)
    read_latency_throughput(dfs, read_throughput)
    read_latency_throughput_errorbar(dfs, read_throughput)

def get_read_throughput(path):
    df = get_data(path, 'readclient-' + LOCAL_REGION)
    return (df.loc[df['logType'] == 'LAST_ROT', 'totalReads'].values[0] / 60).round(2)

def get_latency_times(path):
    df = get_data(path, 'readclient-' + LOCAL_REGION)

    df_result = df.groupby('id').apply(
        lambda group:
            group.loc[group['logType'] == 'ROT_RESPONSE', 'time'].values[0] -
            group.loc[group['logType'] == 'ROT_REQUEST', 'time'].values[0]
    ).reset_index(name='latency')

    return df_result.sort_values('id').reset_index(drop=True)


def latency_distribution_table(test_names, dfs, read_throughput):
    df_results = []
    for i, df in enumerate(dfs):
        df_stats = df_describe(df, 'latency').round(2)
        (r, w) = re.findall(pattern, test_names[i])[0]
        df_stats['read_clients'] = int(r)
        df_stats['write_clients'] = int(w)
        df_stats['read_throughput'] = read_throughput[i]
        df_stats['write_throughput'] = 20 * int(w)
        df_stats['count'] = df.shape[0]
        df_stats['read %'] = (df_stats['read_throughput'] / (df_stats['read_throughput'] + df_stats['write_throughput']) * 100).round(2)
        df_results.append(df_stats)

    df_result = pd.concat(df_results, ignore_index=True).sort_values(by=['read_clients', 'write_clients'])

    plt.figure(figsize=(18, 3))
    plt.table(cellText=df_result.values, colLabels=df_result.columns,
              cellLoc='center', loc='center')
    plt.axis('off')
    plt.annotate('Latency (ms)', (0.5, 0.9), xycoords='axes fraction',
                 ha='center', va='bottom', fontsize=10)
    plt.savefig(RESULT_PATH + '/clients_latency_table.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()

def read_latency_throughput(dfs, read_throughput):
    _, ax = plt.subplots(figsize=(8, 5))
    def estimator_99(data): return np.percentile(data, 99)
    def estimator_95(data): return np.percentile(data, 95)

    df_results = []
    for i, df in enumerate(dfs):
        df_result = df.copy()
        df_result['read_throughput'] = read_throughput[i]
        df_results.append(df_result)

    df_result = pd.concat(df_results, ignore_index=True)
    print(df_result.info())

    sns.lineplot(data=df_result, x="read_throughput", y='latency', marker='X', markersize=10, estimator=estimator_99, errorbar=None, linewidth=3, 
                 markeredgewidth=1, markeredgecolor='w')
    sns.lineplot(data=df_result, x="read_throughput", y='latency', marker='v', markersize=10, estimator=estimator_95, errorbar=None, linewidth=3, 
                 markeredgewidth=1, markeredgecolor='w')
    sns.lineplot(data=df_result, x="read_throughput", y='latency', marker='o', markersize=10, errorbar=None, linewidth=3, 
                 markeredgewidth=1, markeredgecolor='w')

    ax.xaxis.grid(True)
    ax.set_ylabel("Read Latency (ms)", labelpad=5)
    ax.set_xlabel("Read Throughput (1000 x ROT/s)", labelpad=5)
    ax.xaxis.set_major_formatter(ticker.FuncFormatter(lambda x, _: '{0:g}'.format(x / 1000)))
    ax.legend(loc="upper left", labels=['P99', 'P95', 'Avg',])
    plt.savefig(RESULT_PATH + '/' + 'read_latency_throughput.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()

def read_latency_throughput_errorbar(dfs, read_throughput):
    _, ax = plt.subplots(figsize=(8, 5))

    df_results = []
    for i, df in enumerate(dfs):
        df_result = df.copy()
        df_result['read_throughput'] = read_throughput[i]
        df_results.append(df_result)

    df_result = pd.concat(df_results, ignore_index=True)
    print(df_result.info())

    sns.lineplot(data=df_result, x="read_throughput", y='latency', marker='o', markersize=10, linewidth=3, 
                 markeredgewidth=1, markeredgecolor='w')

    ax.xaxis.grid(True)
    ax.set_ylabel("Read Latency (ms)", labelpad=5)
    ax.set_xlabel("Read Throughput (1000 x ROT/s)", labelpad=5)
    ax.xaxis.set_major_formatter(ticker.FuncFormatter(lambda x, _: '{0:g}'.format(x / 1000)))
    plt.savefig(RESULT_PATH + '/' + 'read_latency_throughput_errorbar.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()