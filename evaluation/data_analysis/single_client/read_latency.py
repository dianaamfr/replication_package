import pandas as pd
import seaborn as sns
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
from data_analysis.utils import PATH, CC_DIR, EC_DIR, LOCAL_REGION, DELAYS, PALETTE_SHORT, MARKERS
from data_analysis.utils import get_data, df_describe
import math

EC_RAW_PATH = PATH + '/logs/single_client/read_latency' + EC_DIR + '/d_'
CC_RAW_PATH = PATH + '/logs/single_client/read_latency' + CC_DIR + '/d_'
RESULTS_PATH = PATH + '/results/single_client/read_latency'

def latency_evaluation():
    dfs = []

    for delay in DELAYS:
        df_ec_latency = get_latency_times(EC_RAW_PATH, delay)
        df_ec_latency['consistency'] = 'EC'
        df_ec_latency['delay'] = delay

        df_cc_latency = get_latency_times(CC_RAW_PATH, delay)
        df_cc_latency['consistency'] = 'CC'
        df_cc_latency['delay'] = delay

        dfs.append(df_ec_latency)
        dfs.append(df_cc_latency)

        latency_distribution_table(df_ec_latency, df_cc_latency, delay)

    df = pd.concat(dfs).reset_index(drop=True)
    latency_boxplot(df, outliers=False, interval=5, fig_size=(8, 10))
    latency_boxplot(df, outliers=True, interval=10, fig_size=(8, 10))

    latency_average_barplot(df)

    latency_with_write_delay(df)


def get_latency_times(path, delay):
    df = get_data(path + str(delay), 'readclient-' + LOCAL_REGION)

    return df.groupby('id').apply(
        lambda group:
            group.loc[group['logType'] == 'ROT_RESPONSE', 'time'].values[0] -
            group.loc[group['logType'] == 'ROT_REQUEST', 'time'].values[0]
    ).reset_index(name='latency').tail(100).reset_index(drop=True)


def latency_distribution_table(df_ec_latency, df_cc_latency, delay):
    df_ec_stats = df_describe(df_ec_latency, 'latency').round(2)
    df_cc_stats = df_describe(df_cc_latency, 'latency').round(2)

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
    plt.annotate('Latency (ms)', (0.5, 0.7), xycoords='axes fraction',
                 ha='center', va='bottom', fontsize=10)
    plt.savefig(RESULTS_PATH + '/latency_table_' + str(delay) +
                '.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()


def latency_boxplot(df, outliers=False, interval=5, fig_size=(10, 10)):
    plt.figure(figsize=fig_size)
    sns.boxplot(data=df, x="delay", y="latency",
                hue="consistency", hue_order=['EC','CC'], showfliers=outliers, order=DELAYS)
    plt.gca().xaxis.grid(True)
    plt.gca().yaxis.set_major_locator(ticker.MultipleLocator(base=interval))
    plt.xlabel("Inter-Write Delay (ms)", labelpad=10)
    plt.ylabel("Read Latency (ms)", labelpad=10)
    handles, labels = plt.gca().get_legend_handles_labels()
    plt.legend(handles, [label.capitalize() for label in labels], loc="upper right")
    plt.savefig(RESULTS_PATH + '/latency_boxplot' + ('_outliers' if outliers else '') + '.png', dpi=300)
    plt.clf()
    plt.close()


def latency_average_barplot(df):
    _, ax = plt.subplots(figsize=(10, 5))
    grouped_data = df.groupby(["delay", "consistency"])["latency"]
    average_latency = grouped_data.mean().reset_index()

    sns.barplot(data=average_latency, x="delay", y="latency",
                hue="consistency", hue_order = ['EC','CC'], width=0.6, linewidth=1, edgecolor='black', order=DELAYS, alpha=0.9)
    ax.xaxis.grid(True)
    ax.set_xlabel("Inter-Write Delay (ms)", labelpad=10)
    ax.set_ylabel("Read Latency (ms)", labelpad=10)
    plt.yticks(np.arange(0, math.ceil(average_latency['latency'].max()) + 1, 5))
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, [label.capitalize()
              for label in labels], loc="upper right")
    plt.savefig(RESULTS_PATH + '/latency_barplot.png', dpi=300)
    plt.clf()
    plt.close()


def latency_with_write_delay(df):
    df_ec_mean = df[df['consistency'] == 'EC'].groupby('delay').mean().sort_index().reset_index()
    df_cc_mean = df[df['consistency'] == 'CC'].groupby('delay').mean().sort_index().reset_index()

    df_ec_99 = df[df['consistency'] == 'EC'].groupby('delay')['latency'].apply(lambda x: np.percentile(x, 99)).sort_index().reset_index(name='percentile_99')
    df_cc_99 = df[df['consistency'] == 'CC'].groupby('delay')['latency'].apply(lambda x: np.percentile(x, 99)).sort_index().reset_index(name='percentile_99')

    y_coords_ec_mean, y_coords_cc_mean = [], []
    y_coords_ec_99, y_coords_cc_99 = [], []
    for delay in DELAYS:
        y_coords_ec_mean.append(df_ec_mean[df_ec_mean['delay'] == delay]['latency'].values[0])
        y_coords_cc_mean.append(df_cc_mean[df_cc_mean['delay'] == delay]['latency'].values[0])
        y_coords_ec_99.append(df_ec_99[df_ec_99['delay'] == delay]['percentile_99'].values[0])
        y_coords_cc_99.append(df_cc_99[df_cc_99['delay'] == delay]['percentile_99'].values[0])

    # Average Read Latency
    _, ax = plt.subplots(figsize=(8, 4))

    plt.plot(DELAYS, y_coords_ec_mean, marker=MARKERS[1], markersize=9, linewidth=3, linestyle='-', color=PALETTE_SHORT[0], markeredgewidth=1, markeredgecolor='w')
    plt.plot(DELAYS, y_coords_cc_mean, marker=MARKERS[1], markersize=9, linewidth=3, linestyle='-', color=PALETTE_SHORT[1], markeredgewidth=1, markeredgecolor='w')

    ax.xaxis.grid(True)
    ax.set_xlabel("Inter-Write Delay (ms)", labelpad=10)
    ax.set_ylabel("Average Read Latency (ms)", labelpad=10)
    ax.xaxis.set_major_formatter(plt.FormatStrFormatter('%.0f'))
    plt.yticks(range(0, math.ceil(max(y_coords_cc_mean + y_coords_ec_mean)) + 3, 3))
    plt.legend(['EC', 'CC'], loc="upper right")
    plt.savefig(RESULTS_PATH + '/latency_with_write_delay.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()

    # 99th Percentile Read Latency
    _, ax = plt.subplots(figsize=(8, 6))

    plt.plot(DELAYS, y_coords_ec_99, marker=MARKERS[1], markersize=9, linewidth=3, linestyle='-', color=PALETTE_SHORT[0], markeredgewidth=1, markeredgecolor='w')
    plt.plot(DELAYS, y_coords_cc_99, marker=MARKERS[1], markersize=9, linewidth=3, linestyle='-', color=PALETTE_SHORT[1], markeredgewidth=1, markeredgecolor='w')

    ax.xaxis.grid(True)
    ax.set_xlabel("Inter-Write Delay (ms)", labelpad=10)
    ax.set_ylabel("99th Percentile Read Latency (ms)", labelpad=10)
    ax.xaxis.set_major_formatter(plt.FormatStrFormatter('%.0f'))
    plt.yticks(range(0, math.ceil(max(y_coords_cc_99 + y_coords_ec_99)) + 10, 10))
    plt.legend(['EC', 'CC'], loc="upper right")
    plt.savefig(RESULTS_PATH + '/latency_99_with_write_delay.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()
