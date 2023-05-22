import pandas as pd
import seaborn as sns
import numpy as np
import matplotlib.pyplot as plt
from visualization.utils import PATH, CC_DIR, EC_DIR, LOCAL_REGION, DELAYS, LINESTYLES
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

    goodput_average_barplot(df, 'writes_per_second', 'Goodput (writes/s)')
    goodput_average_barplot(df, 'bytes_per_second', 'Goodput (bytes/s)')

    goodput_latency_relation(df, 'writes_per_second', 'Goodput (erites/s)')
    goodput_latency_relation(df, 'bytes_per_second', 'Goodput (bytes/s)')


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


def goodput_average_barplot(df, goodput_var, ylabel):
    _, ax = plt.subplots(figsize=(7, 6))
    grouped_data = df.groupby(["delay", "consistency"])[goodput_var]
    average_goodput = grouped_data.mean().round(2).reset_index()

    sns.barplot(data=average_goodput, x="delay", y=goodput_var,
                hue="consistency", hue_order = ['EC','CC'], width=0.6, linewidth=1, edgecolor='black', order=DELAYS, alpha=0.9)
    ax.xaxis.grid(True)
    ax.set_xlabel("Inter-Read Delay (ms)", labelpad=10)
    ax.set_ylabel(ylabel, labelpad=10)
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, [label.capitalize()
              for label in labels], loc="upper right")
    plt.savefig(RESULT_PATH + '/' + goodput_var + '_barplot.png', dpi=300)
    plt.clf()


def goodput_latency_relation(df, goodput_var, ylabel):
    _, ax = plt.subplots(figsize=(7, 6))

    def estimator_99(data): return np.percentile(data, 99)
    def estimator_95(data): return np.percentile(data, 95)

    sns.lineplot(data=df, x="delay", y=goodput_var, hue="consistency", style="consistency", markers=['s', 'o'], dashes=[LINESTYLES[0], LINESTYLES[0]],
                 markersize=6, estimator=estimator_99, errorbar=None, linewidth=3, markeredgewidth=1, markeredgecolor='w', hue_order=['EC','CC'])
    sns.lineplot(data=df, x="delay", y=goodput_var, hue="consistency", style="consistency", markers=['s', 'o'], dashes=[LINESTYLES[1], LINESTYLES[1]],
                 markersize=8, estimator=estimator_95, errorbar=None, linewidth=3, markeredgewidth=1, markeredgecolor='w', hue_order=['EC','CC'])
    sns.lineplot(data=df, x="delay", y=goodput_var, hue="consistency", style="consistency", markers=['s', 'o'], dashes=[LINESTYLES[2], LINESTYLES[2]],
                 markersize=10, estimator=np.mean, errorbar=None, linewidth=3, markeredgewidth=1, markeredgecolor='w', hue_order=['EC','CC'])

    ax.xaxis.grid(True)
    ax.set_xlabel("Inter-Read Delay (ms)", labelpad=10)
    ax.set_ylabel(ylabel, labelpad=10)
    ax.xaxis.set_major_formatter(plt.FormatStrFormatter('%.0f'))
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, [label.capitalize()
              for label in labels], loc="lower right")
    ax.legend(loc="upper right", labels=['EC P99', 'CC P99', 'EC P95', 'CC P95', 'EC Avg', 'CC Avg'])
    plt.savefig(RESULT_PATH + '/' + goodput_var + '_with_latency.png', dpi=300)
    plt.clf()
