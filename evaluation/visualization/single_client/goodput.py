import pandas as pd
import seaborn as sns
import numpy as np
import matplotlib.pyplot as plt
from visualization.utils import PATH, CC_DIR, EC_DIR, LOCAL_REGION, DELAYS, COLORS, MARKERS
from visualization.utils import get_data

PAYLOAD_BYTES = 12
EC_GOODPUT_PATH = PATH + '/logs/goodput' + EC_DIR + '/d_'
CC_GOODPUT_PATH = PATH + '/logs/goodput' + CC_DIR + '/d_'

RESULT_PATH = PATH + '/results/goodput'


def goodput_evaluation():
    dfs = []

    for delay in DELAYS:
        df_ec_goodput = get_goodput_times(EC_GOODPUT_PATH, delay)
        df_cc_goodput = get_goodput_times(CC_GOODPUT_PATH, delay)

        goodput_distribution_table(df_ec_goodput, df_cc_goodput, delay)

        df_ec_goodput['consistency'] = 'EC'
        df_ec_goodput['delay'] = delay
        df_cc_goodput['consistency'] = 'CC'
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
    df_ec_stats.index = ['EC']
    df_cc_stats.index = ['CC']

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
    plt.savefig(RESULT_PATH + '/goodput_table_' + str(delay) +
                '.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()


def goodput_average_barplot(df):
    _, ax = plt.subplots(figsize=(7, 5))
    grouped_data = df.groupby(["delay", "consistency"])['writes_per_second']
    average_goodput = grouped_data.mean().round(2).reset_index()

    sns.barplot(data=average_goodput, x="delay", y='writes_per_second',
                hue="consistency", hue_order = ['EC','CC'], width=0.6, linewidth=1, edgecolor='black', order=DELAYS, alpha=0.9)
    ax.xaxis.grid(True)
    ax.set_xlabel("Inter-Read Delay (ms)", labelpad=10)
    ax.set_ylabel('Average Write Goodput (writes/s)', labelpad=10)
    plt.yscale('log')

    max_value = average_goodput['writes_per_second'].max()
    y_max = max_value * 2
    ax.set_ylim(1, y_max)

    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, [label.capitalize()
              for label in labels], loc="upper right")
    plt.savefig(RESULT_PATH + '/' + 'writes_per_second' + '_barplot.png', dpi=300)
    plt.clf()
    plt.close()


def goodput_with_read_delay(df):
    df_ec_mean = df[df['consistency'] == 'EC'].groupby('delay').mean().sort_index().reset_index()
    df_cc_mean = df[df['consistency'] == 'CC'].groupby('delay').mean().sort_index().reset_index()

    y_coords_ec_mean, y_coords_cc_mean = [], []
    for delay in DELAYS:
        y_coords_ec_mean.append(df_ec_mean[df_ec_mean['delay'] == delay]['writes_per_second'].values[0])
        y_coords_cc_mean.append(df_cc_mean[df_cc_mean['delay'] == delay]['writes_per_second'].values[0])

    _, ax = plt.subplots(figsize=(8, 5))
        
    plt.plot(DELAYS, y_coords_ec_mean, marker=MARKERS[1], markersize=12, linewidth=3, linestyle='-', color=COLORS[0], markeredgewidth=1, markeredgecolor='w')
    plt.plot(DELAYS, y_coords_cc_mean, marker=MARKERS[1], markersize=12, linewidth=3, linestyle='-', color=COLORS[1], markeredgewidth=1, markeredgecolor='w')

    ax.xaxis.grid(True)
    ax.set_xlabel("Inter-Read Delay (ms)", labelpad=10)
    ax.set_ylabel("Average Write Goodput (writes/s)", labelpad=10)
    ax.xaxis.set_major_formatter(plt.FormatStrFormatter('%.0f'))
    plt.yscale('log')

    max_value = max(y_coords_cc_mean + y_coords_ec_mean).max()
    y_max = max_value * 2
    ax.set_ylim(1, y_max)

    plt.legend(['EC', 'CC'], loc="upper right")
    plt.savefig(RESULT_PATH + '/goodput_with_read_delay.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()
