import pandas as pd
import seaborn as sns
import numpy as np
import matplotlib.pyplot as plt
from utils import PATH, CC_DIR, EC_DIR, LOCAL_REGION, DELAYS, PALETTE_SHORT, MARKERS, PAYLOAD_BYTES
from utils import get_data

EC_RAW_PATH = PATH + '/logs/single_client/goodput' + EC_DIR + '/d_'
CC_RAW_PATH = PATH + '/logs/single_client/goodput' + CC_DIR + '/d_'
RESULTS_PATH = PATH + '/results/single_client/goodput'

def goodput_evaluation():
    dfs = []

    for delay in DELAYS:
        df_ec_goodput = get_goodput_times(EC_RAW_PATH, delay)
        df_cc_goodput = get_goodput_times(CC_RAW_PATH, delay)

        goodput_distribution_table(df_ec_goodput, df_cc_goodput, delay)

        df_ec_goodput['consistency'] = 'Baseline'
        df_ec_goodput['delay'] = delay
        df_cc_goodput['consistency'] = 'Prototype'
        df_cc_goodput['delay'] = delay

        dfs.append(df_ec_goodput)
        dfs.append(df_cc_goodput)

    df = pd.concat(dfs).reset_index(drop=True)

    goodput_average_barplot(df)

    goodput_with_read_delay(df)


def get_goodput_times(path, delay):
    df = get_data(path + str(delay), 'readclient-' + LOCAL_REGION)

    df_result = pd.DataFrame()
    df_result['elapsed_time'] = df['time']
    df_result['total_writes'] = df['totalVersions']
    df_result['total_bytes'] = (df_result['total_writes'] * PAYLOAD_BYTES)
    df_result['writes_per_second'] = (
        (1000 * df_result['total_writes']) / df_result['elapsed_time'])
    df_result['bytes_per_second'] = (
        df_result['total_bytes'] / df_result['elapsed_time'])

    return df_result


def goodput_distribution_table(df_ec_goodput, df_cc_goodput, delay):
    df_ec_stats = df_ec_goodput.mean().to_frame().transpose().round(2)
    df_cc_stats = df_cc_goodput.mean().to_frame().transpose().round(2)
    df_ec_stats.index = ['Baseline']
    df_cc_stats.index = ['Prototype']

    df_result = pd.concat([df_ec_stats, df_cc_stats])
    df_result['consistency'] = df_result.index
    df_result = df_result.reset_index(drop=True)

    df_result = pd.concat(
        [df_result['consistency'], df_result.drop('consistency', axis=1)], axis=1)

    plt.table(cellText=df_result.values, colLabels=df_result.columns,
              cellLoc='center', loc='center')
    plt.axis('off')
    plt.annotate('Goodput', (0.5, 0.7), xycoords='axes fraction',
                 ha='center', va='bottom', fontsize=10)
    plt.savefig(RESULTS_PATH + '/goodput_table_' + str(delay) +
                '.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()


def goodput_average_barplot(df):
    df_result = df.copy()
    _, ax = plt.subplots(figsize=(5.5, 4))
    grouped_data = df_result.groupby(["delay", "consistency"])['writes_per_second']
    average_goodput = grouped_data.mean().round(2).reset_index()

    sns.barplot(data=average_goodput, x="delay", y='writes_per_second',
                hue="consistency", hue_order = ['Baseline','Prototype'], width=0.6, linewidth=1, edgecolor='black', order=DELAYS, alpha=0.9)
    ax.xaxis.grid(True)
    ax.set_xlabel("Inter-Read Delay (ms)", labelpad=8, fontsize=16)
    ax.set_ylabel('Average Goodput (writes/s)', labelpad=8, fontsize=16)
    plt.yscale('log')

    max_value = average_goodput['writes_per_second'].max()
    y_max = max_value * 2
    ax.set_ylim(1, y_max)

    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, [label.capitalize()
              for label in labels], loc="upper right", fontsize=13)
    plt.tick_params(axis='x', labelsize=14)
    plt.tick_params(axis='y', labelsize=14)
    plt.savefig(RESULTS_PATH + '/writes_per_second_barplot.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()


def goodput_with_read_delay(df):
    df_ec_mean = df[df['consistency'] == 'Baseline'].groupby('delay').mean().sort_index().reset_index()
    df_cc_mean = df[df['consistency'] == 'Prototype'].groupby('delay').mean().sort_index().reset_index()

    y_coords_ec_mean, y_coords_cc_mean = [], []
    for delay in DELAYS:
        y_coords_ec_mean.append(df_ec_mean[df_ec_mean['delay'] == delay]['writes_per_second'].values[0])
        y_coords_cc_mean.append(df_cc_mean[df_cc_mean['delay'] == delay]['writes_per_second'].values[0])

    _, ax = plt.subplots(figsize=(5.5, 4))
        
    plt.plot(DELAYS, y_coords_ec_mean, marker=MARKERS[1], markersize=9, linewidth=3, linestyle='-', color=PALETTE_SHORT[0], markeredgewidth=1, markeredgecolor='w')
    plt.plot(DELAYS, y_coords_cc_mean, marker=MARKERS[1], markersize=9, linewidth=3, linestyle='-', color=PALETTE_SHORT[1], markeredgewidth=1, markeredgecolor='w')

    ax.xaxis.grid(True)
    ax.set_xlabel("Inter-Read Delay (ms)", labelpad=8, fontsize=16)
    ax.set_ylabel("Average Goodput (writes/s)", labelpad=8, fontsize=16)
    ax.xaxis.set_major_formatter(plt.FormatStrFormatter('%.0f'))
    plt.yscale('log')

    max_value = max(y_coords_cc_mean + y_coords_ec_mean).max()
    y_max = max_value * 2
    ax.set_ylim(1, y_max)

    plt.legend(['Baseline', 'Prototype'], loc="upper right", fontsize=13)
    plt.tick_params(axis='x', labelsize=13)
    plt.tick_params(axis='y', labelsize=13)
    plt.savefig(RESULTS_PATH + '/goodput_with_read_delay.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()
