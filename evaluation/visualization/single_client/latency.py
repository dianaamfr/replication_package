import pandas as pd
import seaborn as sns
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
from visualization.utils import PATH, CC_DIR, EC_DIR, LOCAL_REGION, DELAYS, LINESTYLES, MARKERS
from visualization.utils import get_data, df_describe

EC_LATENCY_PATH = PATH + '/logs/latency' + EC_DIR + '/d_'
CC_LATENCY_PATH = PATH + '/logs/latency' + CC_DIR + '/d_'

RESULT_PATH = PATH + '/results/latency'


def latency_evaluation():
    dfs = []

    for delay in DELAYS:
        df_ec_latency = get_latency_times(EC_LATENCY_PATH, delay)
        df_ec_latency['consistency'] = 'EC'
        df_ec_latency['delay'] = delay

        df_cc_latency = get_latency_times(CC_LATENCY_PATH, delay)
        df_cc_latency['consistency'] = 'CC'
        df_cc_latency['delay'] = delay

        dfs.append(df_ec_latency)
        dfs.append(df_cc_latency)

        latency_distribution_table(df_ec_latency, df_cc_latency, delay)

    df = pd.concat(dfs).reset_index(drop=True)
    latency_boxplot(df, outliers=False, interval=5, fig_size=(7, 10))
    latency_boxplot(df, outliers=True, interval=10, fig_size=(8, 15))

    # latency_histogram(df)

    latency_average_barplot(df)

    latency_throughput_relation(df)
    latency_throughput_relation_errorbar(df)


# Univariate
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
    plt.savefig(RESULT_PATH + '/latency_table_' + str(delay) +
                '.png', dpi=300, bbox_inches='tight')
    plt.clf()


def latency_boxplot(df, outliers=False, interval=5, fig_size=(10, 10)):
    plt.figure(figsize=fig_size)
    sns.boxplot(data=df, x="delay", y="latency",
                hue="consistency", hue_order=['EC','CC'], showfliers=outliers, order=DELAYS)
    plt.gca().xaxis.grid(True)
    plt.gca().yaxis.set_major_locator(ticker.MultipleLocator(base=interval))
    plt.xlabel("Inter-Write Delay (writes/s)", labelpad=10)
    plt.ylabel("Read Latency (ms)", labelpad=10)
    handles, labels = plt.gca().get_legend_handles_labels()
    plt.legend(handles, [label.capitalize() for label in labels], loc="upper right")
    plt.savefig(RESULT_PATH + '/latency_boxplot' + ('_outliers' if outliers else '') + '.png', dpi=300)
    plt.clf()

# def latency_histogram(df):
#     g = sns.FacetGrid(df, row="delay", hue="consistency", height=10, aspect=4, margin_titles=True)
#     g.map(sns.histplot, "latency", kde=True)
#     g.set(yscale='symlog')
#     g.add_legend()
#     plt.savefig(RESULT_PATH + '/latency_histogram.png', dpi=300)
#     plt.clf()


def latency_average_barplot(df):
    _, ax = plt.subplots(figsize=(10, 10))
    grouped_data = df.groupby(["delay", "consistency"])["latency"]
    average_latency = grouped_data.mean().reset_index()

    sns.barplot(data=average_latency, x="delay", y="latency",
                hue="consistency", hue_order = ['EC','CC'], width=0.6, linewidth=1, edgecolor='black', order=DELAYS, alpha=0.9)
    ax.xaxis.grid(True)
    ax.set_xlabel("Inter-Write Delay (writes/s)", labelpad=10)
    ax.set_ylabel("Read Latency (ms)", labelpad=10)
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, [label.capitalize()
              for label in labels], loc="lower right")
    plt.savefig(RESULT_PATH + '/latency_barplot.png', dpi=300)
    plt.clf()

# Multivariate


def latency_throughput_relation(df):
    _, ax = plt.subplots(figsize=(8, 8))
    def estimator_99(data): return np.percentile(data, 99)
    def estimator_95(data): return np.percentile(data, 95)

    sns.lineplot(data=df, x="delay", y="latency", hue="consistency", style="consistency", markers=MARKERS, dashes=[LINESTYLES[0], LINESTYLES[0]],
                 markersize=8, estimator=estimator_99, errorbar=None, linewidth=3, markeredgewidth=1,
                markeredgecolor='w', hue_order=['EC','CC'])
    sns.lineplot(data=df, x="delay", y="latency", hue="consistency", style="consistency", markers=MARKERS, dashes=[LINESTYLES[1], LINESTYLES[1]],
                 markersize=9, estimator=estimator_95, errorbar=None, linewidth=3, markeredgewidth=1,
                markeredgecolor='w', hue_order=['EC','CC'])
    sns.lineplot(data=df, x="delay", y="latency", hue="consistency", style="consistency", markers=MARKERS, dashes=[LINESTYLES[2], LINESTYLES[2]],
                 markersize=10, estimator=np.mean, errorbar=None, linewidth=3, markeredgewidth=1, 
                 markeredgecolor='w', hue_order=['EC','CC'])

    ax.xaxis.grid(True)
    ax.set_xlabel("Inter-Write Delay (writes/s)", labelpad=10)
    ax.set_ylabel("Read Latency (ms)", labelpad=10)
    ax.xaxis.set_major_formatter(plt.FormatStrFormatter('%.0f'))
    ax.legend(loc="upper right", labels=['EC P99', 'CC P99', 'EC P95', 'CC P95', 'EC Avg', 'CC Avg'])
    plt.savefig(RESULT_PATH + '/latency_with_throughput.png', dpi=300)
    plt.clf()

def latency_throughput_relation_errorbar(df):
    _, ax = plt.subplots(figsize=(8, 8))

    sns.lineplot(data=df, x="delay", y="latency", hue="consistency", style="consistency", markers=MARKERS,
                 markersize=10, linewidth=2, markeredgewidth=1, markeredgecolor='w', hue_order=['EC','CC'])

    ax.xaxis.grid(True)
    ax.set_xlabel("Inter-Write Delay (writes/s)", labelpad=10)
    ax.set_ylabel("Average Read Latency (ms)", labelpad=10)
    ax.xaxis.set_major_formatter(plt.FormatStrFormatter('%.0f'))
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, [label.capitalize()
              for label in labels], loc="lower right")
    plt.savefig(RESULT_PATH + '/latency_with_throughput_errorbar.png', dpi=300)
    plt.clf()