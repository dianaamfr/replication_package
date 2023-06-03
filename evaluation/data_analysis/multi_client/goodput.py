from data_analysis.utils import get_data
from data_analysis.utils import PATH, LOCAL_REGION
import os
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import math

RAW_PATH = PATH + '/logs/multi_client/goodput'
RESULT_PATH = PATH + '/results/multi_client/goodput'

def goodput_evaluation():
    df = get_goodput_times()
    goodput_with_partitions(df)


def get_goodput_times():
    partitions = [name for name in os.listdir(RAW_PATH) if os.path.isdir(os.path.join(RAW_PATH, name))]
    dfs = []
    for partition in partitions:
        partition_dir = os.path.join(RAW_PATH, partition)
        subdirs = [name for name in os.listdir(partition_dir) if os.path.isdir(os.path.join(partition_dir, name))]
        
        writes_per_second_results = []
        for subdir in subdirs:
            read_client_df = get_data(os.path.join(partition_dir, subdir), 'readclient-' + LOCAL_REGION)
            read_node_df = get_data(os.path.join(partition_dir, subdir), 'readnode-' + LOCAL_REGION + '-s3') 

            last_stable_time = get_last_stable_time(read_client_df)

            time = last_stable_time['time']
            writes = get_stable_versions(read_node_df, last_stable_time['lastStableTime'])['versions']
            writes_per_second_results.append(((1000 * writes) / time))

        dfs.append((partition, (sum(writes_per_second_results) / len(writes_per_second_results)).round(2)))

    return pd.DataFrame(dfs, columns=['partitions', 'writes_per_second'])


def get_stable_versions(df, lastStableTime):
    return df[(df['logType'] == 'STABLE_TIME_VERSIONS') & (df['stableTime'] == lastStableTime)].iloc[0]


def get_last_stable_time(df):
    return df[(df['logType'] == 'LAST_STABLE_TIME')].iloc[0]


def goodput_with_partitions(df):
    df = df.sort_values(by=['partitions']).reset_index(drop=True)

    # Table
    plt.table(cellText=df.values, colLabels=df.columns, cellLoc='center', loc='center')
    plt.axis('off')
    plt.annotate('Goodput', (0.5, 0.7), xycoords='axes fraction', ha='center', va='bottom', fontsize=10)
    plt.savefig(RESULT_PATH + '/goodput_table_partitions.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()

    # Barplot
    _, ax = plt.subplots(figsize=(5, 7))
    sns.barplot(x='partitions', y='writes_per_second', data=df, ax=ax, width=0.6, linewidth=1, edgecolor='black', alpha=0.9)
    ax.set_yscale('log')
    ax.set_xlabel('Partitions')
    ax.set_ylim(0, math.pow(10, 4))
    ax.set_ylabel('Goodput (writes/s)')
    plt.savefig(RESULT_PATH + '/goodput_barplot_partitions.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()