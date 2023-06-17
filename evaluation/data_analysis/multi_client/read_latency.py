import os
import re
import numpy as np
import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
from utils import PATH, LOCAL_REGION, MARKERS, PALETTE_FULL
from utils import get_data, df_describe
import math

RAW_PATH = PATH + '/logs/multi_client/read_times/partitions'
RESULTS_PATH = PATH + '/results/multi_client/read_latency'
pattern = r"r(\d+)_w(\d+)"

def latency_evaluation():
    latency_with_clients('32cores_12keys')
    latency_with_clients('32cores_8keys')
    reads_with_clients_barplot('32cores_8keys')
    latency_with_partitions('32cores_8keys', 'r5_w10')


def latency_with_clients(subdir):
    dir = os.path.join(RAW_PATH, 'p1', subdir)
    test_names = [name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]
    dfs = []
    read_throughput = []
    for test_name in test_names:
        file_path = os.path.join(dir, test_name)
        df = get_latency_times(file_path)
        dfs.append(df)
        read_throughput.append(get_read_throughput(file_path))

    latency_distribution(test_names, dfs, read_throughput, subdir)


def reads_with_clients_barplot(subdir):
    dir = os.path.join(RAW_PATH, 'p1', subdir)
    test_names = [name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]
    dfs = []
    for test_name in test_names:
        file_path = os.path.join(dir, test_name)
        df = get_latency_times(file_path)
        (r, w) = re.findall(pattern, test_name)[0]
        df['read_clients'] = int(r)
        df['write_clients'] = int(w)
        read_throughput = get_read_throughput(file_path)
        write_throughput = 20 * int(w)
        df['read_throughput'] = read_throughput
        df['read_percentage'] = (read_throughput / (read_throughput + write_throughput)).round(3) * 100
        df['rw'] = r + ':' + w
        dfs.append(df)

    df_result = pd.concat(dfs, ignore_index=True).sort_values(by=['read_clients', 'write_clients'])
    df_avg = df_result.groupby(['read_clients', 'write_clients']).mean().round(2).reset_index()
    read_percentage = list(df_avg['read_percentage'])
    
    # Read Latency with Read Percentage
    _, ax = plt.subplots(figsize=(8, 5))
    sns.barplot(x='rw', y='latency', data=df_result, ax=ax, width=0.6, linewidth=1, edgecolor='black', alpha=0.9, palette=PALETTE_FULL, errorbar=None, zorder=1)
    ax.set_xlabel("Readers : Writers", labelpad=5)
    ax.set_ylabel('Average Latency (ms)', labelpad=5)

    ax2 = ax.twiny()
    ax2.set_xlim(ax.get_xlim())
    ax2.set_xticks(np.arange(len(read_percentage)))
    ax2.set_xlabel("Read %", labelpad=5)
    ax2.set_xticklabels(read_percentage)
    ax.grid(True, which='both', axis='y', zorder=0)
    ax2.grid(False)

    plt.savefig(RESULTS_PATH + '/' + 'latency_with_clients_barplot.png', dpi=300)
    plt.clf()
    plt.close()


def get_read_throughput(path):
    df = get_data(path, 'readclient-' + LOCAL_REGION)
    return (df.loc[df['logType'] == 'THROUGHPUT', 'total'].values[0] / 60).round(2)


def get_latency_times(path):
    df = get_data(path, 'readclient-' + LOCAL_REGION)

    df_result = df.groupby('id').apply(
        lambda group:
            group.loc[group['logType'] == 'ROT_RESPONSE', 'time'].values[0] -
            group.loc[group['logType'] == 'ROT_REQUEST', 'time'].values[0]
    ).reset_index(name='latency')

    return df_result.sort_values('id').reset_index(drop=True)


def latency_distribution(test_names, dfs, read_throughput, filename):
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

    # Table
    plt.figure(figsize=(10, 3))
    plt.table(cellText=df_result.values, colLabels=df_result.columns,
              cellLoc='center', loc='center')
    plt.axis('off')
    plt.annotate('Latency (ms)', (0.5, 0.9), xycoords='axes fraction',
                 ha='center', va='bottom', fontsize=10)
    plt.savefig(RESULTS_PATH + '/clients_latency_table_' + filename + '.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()

    # Scatterplot
    _, ax = plt.subplots(figsize=(6, 3))
    
    y_coords = []
    x_coords = []
    for(_, row) in df_result.iterrows():
        y_coords.append(row['read_throughput'])
        x_coords.append(row['mean'])

    # Average Read Latency
    plt.plot(x_coords, y_coords, marker=MARKERS[1], markersize=8, linewidth=2, linestyle='-', color=PALETTE_FULL[0], markeredgewidth=1, markeredgecolor='w')

    ax.xaxis.grid(True)
    ax.set_ylabel(" (1000 x ROT/s)", labelpad=10)
    ax.set_xlabel("Average Read Latency (ms)", labelpad=10)

    ax.yaxis.set_major_formatter(ticker.FuncFormatter(lambda x, pos: '{:.0f}'.format(x/1000)))
    plt.yticks(range(0, math.ceil(max(y_coords)) + 2000, 2000))

    plt.xticks(np.arange(1, math.ceil(max(x_coords)) + 0.5, 0.5), labels=[int(x) if x.is_integer() else '' for x in np.arange(1, math.ceil(max(x_coords)) + 0.5, 0.5)])

    plt.savefig(RESULTS_PATH + '/clients_latency_plot_avg_' + filename + '.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()


def latency_with_partitions(subdir, test_name):
    dfs = []
    partition_dirs = [name for name in os.listdir(RAW_PATH) if os.path.isdir(os.path.join(RAW_PATH, name))]
    for partition_dir in partition_dirs:
        file_path = os.path.join(RAW_PATH, partition_dir , subdir, test_name)
        df = get_latency_times(file_path)
        df['partitions'] = re.findall(r"p(\d+)", partition_dir)[0]
        dfs.append(df)

    df_result = pd.concat(dfs, ignore_index=True).reset_index(drop=True)
    
    _, ax = plt.subplots(figsize=(5, 6))
    sns.boxplot(data=df_result, x="partitions", y="latency", showfliers=True)
    ax.xaxis.grid(True)
    ax.set_xlabel("Partitions", labelpad=5)
    ax.set_ylabel("Read Latency (ms)", labelpad=5)
    ax.set_yticks(range(0, math.ceil(df_result['latency'].max()) + 2, 2))
  
    plt.savefig(RESULTS_PATH + '/latency_with_partitions_' + subdir + '.png', dpi=300)
    plt.clf()
    plt.close()
