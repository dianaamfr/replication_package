import pandas as pd
import seaborn as sns
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
from utils import PATH, CC_DIR, EC_DIR, LOCAL_REGION, DELAYS, PERCENTILES_FLOAT, PERCENTILES
from utils import get_data, df_describe
import math 

EC_LATENCY_PATH = PATH + '/logs/latency' + EC_DIR + '/d_'
CC_LATENCY_PATH = PATH + '/logs/latency' + CC_DIR + '/d_'

RESULT_PATH = PATH + '/results/latency'

def latency_evaluation():
    dfs = []

    for delay in DELAYS:
        df_ev_latency = get_latency_times(EC_LATENCY_PATH, delay)
        df_ev_latency['consistency'] = 'EC'
        df_ev_latency['goodput'] = "{:.0f}".format(1000/delay)
                                 
        df_cc_latency = get_latency_times(CC_LATENCY_PATH, delay)
        df_cc_latency['consistency'] = 'CC'
        df_cc_latency['goodput'] = "{:.0f}".format(1000/delay)

        dfs.append(df_ev_latency)
        dfs.append(df_cc_latency)

        # Latency distribution table
        latency_distribution_table(df_ev_latency, df_cc_latency, delay)

    # Latency distribution boxplots
    df = pd.concat(dfs)
    latency_boxplot(df, outliers=False, interval=5, fig_size=(15, 7))
    latency_boxplot(df, outliers=True, interval=10, fig_size=(20, 7))

    # TODO: rethink the scales of this plot
    # Latency histogram
    # latency_histogram(df)

    # Latency barplot
    latency_average_barplot(df)

    # Latency throughput relation
    latency_throughput_relation(df)


# Univariate
def get_latency_times(path, delay):
    df = get_data(path + str(delay), 'readclient-' + LOCAL_REGION)

    return df.groupby('id').apply(
        lambda group: 
            group.loc[group['logType'] == 'ROT_RESPONSE', 'time'].values[0] - 
            group.loc[group['logType'] == 'ROT_REQUEST', 'time'].values[0]
        ).reset_index(name='latency').tail(100).reset_index(drop=True)

def latency_distribution_table(df_ev_latency, df_cc_latency, delay): 
    df_ev_stats = df_describe(df_ev_latency, 'latency').round(2)
    df_cc_stats = df_describe(df_cc_latency, 'latency').round(2)

    df_ev_stats.index = ['EC']
    df_cc_stats.index = ['CC']

    df_result = pd.concat([df_ev_stats, df_cc_stats])
    df_result['consistency'] = df_result.index
    df_result = df_result.reset_index(drop=True)

    df_result = pd.concat([df_result['consistency'], df_result.drop('consistency', axis=1)], axis=1)

    plt.table(cellText=df_result.values, colLabels=df_result.columns, cellLoc='center', loc='center')
    plt.axis('off')
    plt.annotate('Latency (ms)', (0.5, 0.7), xycoords='axes fraction', ha='center', va='bottom', fontsize=10)
    plt.savefig(RESULT_PATH + '/latency_table_' + str(delay) + '.png', dpi=300, bbox_inches='tight')
    plt.clf()

def latency_boxplot(df, outliers = False, interval = 5, fig_size = (10, 10)):
    _, ax = plt.subplots(figsize=fig_size)
    sns.boxplot(data=df, x="latency", y="goodput", hue="consistency", showfliers=outliers)
    ax.xaxis.grid(True)
    ax.xaxis.set_major_locator(ticker.MultipleLocator(base=interval))
    ax.set_xlabel(ax.get_xlabel().capitalize(), labelpad=10)
    ax.set_ylabel(ax.get_ylabel().capitalize(), labelpad=10)
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, [label.capitalize() for label in labels], loc="lower right")
    plt.savefig(RESULT_PATH + '/latency_boxplot' + ('_outliers' if outliers else '') + '.png', dpi=300)
    plt.clf()

def latency_histogram(df):
    g = sns.FacetGrid(df, col="goodput", hue="consistency", height=4, aspect=4, margin_titles=True)
    g.map(sns.histplot, "latency", kde=True)
    g.add_legend()
    plt.savefig(RESULT_PATH + '/latency_histogram.png', dpi=300)
    plt.clf()

def latency_average_barplot(df):
    _, ax = plt.subplots(figsize=(10, 10))
    grouped_data = df.groupby(["goodput", "consistency"])["latency"]
    average_latency = grouped_data.mean().reset_index()

    sns.barplot(data=average_latency, x="goodput", y="latency", hue="consistency", width=0.8, edgecolor="#2a2a2a", linewidth=1.5, order=[5, 10, 20])
    ax.xaxis.grid(True)
    ax.set_xlabel(ax.get_xlabel().capitalize(), labelpad=10)
    ax.set_ylabel(ax.get_ylabel().capitalize(), labelpad=10)
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, [label.capitalize() for label in labels], loc="lower right")
    plt.savefig(RESULT_PATH + '/latency_barplot.png', dpi=300)
    plt.clf() 

# Multivariate
def latency_throughput_relation(df):
    _, ax = plt.subplots(figsize=(10, 10))

    estimator_99 = lambda data: np.percentile(data, 99)
    estimator_95 = lambda data: np.percentile(data, 95)
    linestyles = [
        (1, 1),
        (3, 5, 1, 5), 
        (5, 5)]

    sns.lineplot(data=df, x="goodput", y="latency", hue="consistency", style="consistency", markers=['s', 'o'], dashes=[linestyles[0], linestyles[0]],
                 markersize=6, estimator=estimator_99, errorbar=None, linewidth=2, legend=False)
    sns.lineplot(data=df, x="goodput", y="latency", hue="consistency", style="consistency", markers=['s', 'o'], dashes=[linestyles[1], linestyles[1]],
                 markersize=8, estimator=estimator_95, errorbar=None, linewidth=2, legend=False)
    sns.lineplot(data=df, x="goodput", y="latency", hue="consistency", style="consistency", markers=['s', 'o'], dashes=[linestyles[2], linestyles[2]],
                 markersize=10, estimator=np.mean, errorbar=None, linewidth=2, legend=False)

    ax.xaxis.grid(True)
    ax.set_xlabel(ax.get_xlabel().capitalize(), labelpad=10)
    ax.set_ylabel(ax.get_ylabel().capitalize(), labelpad=10)
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, [label.capitalize() for label in labels], loc="lower right")
    plt.savefig(RESULT_PATH + '/latency_with_throughput.png', dpi=300)
    plt.clf()
