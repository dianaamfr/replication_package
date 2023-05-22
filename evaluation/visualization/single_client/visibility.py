import pandas as pd
import seaborn as sns
import matplotlib.ticker as ticker
import matplotlib.pyplot as plt
import numpy as np
from visualization.utils import PATH, CC_DIR, EC_DIR, LOCAL_REGION, REMOTE_REGION, DELAYS, LINESTYLES, COLORS, MARKERS
from visualization.utils import get_data, get_diff, df_describe

EC_VISIBILITY_PATH = PATH + '/logs/visibility' + EC_DIR + '/d_'
CC_VISIBILITY_PATH = PATH + '/logs/visibility' + CC_DIR + '/d_'
RESULT_PATH = PATH + '/results/visibility'


def visibility_evaluation():
    dfs = []
    for delay in DELAYS:
        df_ec_eu, df_ec_us = get_ec_visibility_times(delay)
        df_cc_eu, df_cc_us = get_cc_visibility_times(delay)

        visibility_distribution_tables(
            [df_ec_eu, df_cc_eu, df_ec_us, df_cc_us], delay)

        for df in [df_ec_eu, df_cc_eu, df_ec_us, df_cc_us]:
            dfs.append(df)

    df = pd.concat(dfs).reset_index(drop=True)
    df_cc = pd.concat(dfs[1::2]).reset_index(drop=True)

    visibility_boxplot(df, False)

    write_response_boxplot(df, False)

    stable_time_boxplot(df_cc)

    visibility_barplot(df)
    visibility_barplot(df, 'symlog')

    # TODO: plot showing the response time, push time, pull time, stable time and visibility time of each write

    visibility_throughput_relation(df)
    visibility_throughput_relation_errorbar(df)


def get_ec_visibility_times(delay):
    write_client_df = get_data(
        EC_VISIBILITY_PATH + str(delay), 'writeclient-' + LOCAL_REGION)
    read_client_eu = get_data(
        EC_VISIBILITY_PATH + str(delay), 'readclient-' + LOCAL_REGION)
    read_client_us = get_data(
        EC_VISIBILITY_PATH + str(delay), 'readclient-' + REMOTE_REGION)

    pd_result_eu = pd.DataFrame()
    pd_result_us = pd.DataFrame()

    for i in range(0, len(write_client_df), 2):
        client_request = write_client_df.iloc[i]
        client_response = write_client_df.iloc[i+1]

        # Get request time
        request_time = client_request['time']

        # Get response time
        response_time = client_response['time']

        # Get the time when the version was first read in each read client
        read_time_eu = get_read_time_ec(read_client_eu, client_response['id'])
        read_time_us = get_read_time_ec(read_client_us, client_response['id'])

        pd_result_eu = pd_result_eu.append({
            'version': client_response['id'],
            'client_request': request_time,
            'client_response': response_time,
            'read_client_response': read_time_eu,
        }, ignore_index=True)

        pd_result_us = pd_result_us.append({
            'version': client_response['id'],
            'client_request': request_time,
            'client_response': response_time,
            'read_client_response': read_time_us,
        }, ignore_index=True)

    pd_result_eu = get_time_diff_ec(
        pd_result_eu.tail(100)).reset_index(drop=True)
    pd_result_eu['consistency'] = 'EC'
    pd_result_eu['delay'] = delay
    pd_result_eu['region'] = LOCAL_REGION

    pd_result_us = get_time_diff_ec(
        pd_result_us.tail(100)).reset_index(drop=True)
    pd_result_us['consistency'] = 'EC'
    pd_result_us['delay'] = delay
    pd_result_us['region'] = REMOTE_REGION

    return pd_result_eu.reset_index(drop=True), pd_result_us.reset_index(drop=True)


def get_cc_visibility_times(delay):
    write_client_df = get_data(
        CC_VISIBILITY_PATH + str(delay), 'writeclient-' + LOCAL_REGION)
    write_node_df = get_data(CC_VISIBILITY_PATH + str(delay), 'writenode-1')
    pusher_df = get_data(CC_VISIBILITY_PATH + str(delay), 'writenode-1-s3')
    puller_eu_df = get_data(CC_VISIBILITY_PATH + str(delay),
                            'readnode-' + LOCAL_REGION + '-s3')
    puller_us_df = get_data(CC_VISIBILITY_PATH + str(delay),
                            'readnode-' + REMOTE_REGION + '-s3')
    read_client_eu = get_data(
        CC_VISIBILITY_PATH + str(delay), 'readclient-' + LOCAL_REGION)
    read_client_us = get_data(
        CC_VISIBILITY_PATH + str(delay), 'readclient-' + REMOTE_REGION)

    pd_result_eu = pd.DataFrame()
    pd_result_us = pd.DataFrame()

    for i in range(0, len(write_client_df), 2):
        client_request = write_client_df.iloc[i]
        client_response = write_client_df.iloc[i+1]

        # Get request time
        request_time = client_request['time']

        # Get response time
        response_time = client_response['time']

        # Get the time when the version was first pushed in the write node
        push_time = get_push_time(pusher_df, client_response['version'])

        # Get the time when the version was first pulled in each read node
        pull_time_eu = get_pull_time(puller_eu_df, client_response['version'])
        pull_time_us = get_pull_time(puller_us_df, client_response['version'])

        # Get the time when the version was stored in each read node
        store_time_eu = get_store_time(
            puller_eu_df, client_response['version'])
        store_time_us = get_store_time(
            puller_us_df, client_response['version'])

        # Get the time where the version became stable in each read node
        stable_time_eu = get_stable_time(
            puller_eu_df, client_response['version'])
        stable_time_us = get_stable_time(
            puller_us_df, client_response['version'])

        # Get the time when the version was first read in each read client
        read_time_eu = get_read_time_cc(
            read_client_eu, client_response['version'])
        read_time_us = get_read_time_cc(
            read_client_us, client_response['version'])

        pd_result_eu = pd_result_eu.append({
            'version': client_response['version'],
            'client_request': request_time,
            'write_node_receive': write_node_df.iloc[i]['time'],
            'write_node_respond': write_node_df.iloc[i+1]['time'],
            'client_response': response_time,
            'write_node_push': push_time,
            'read_node_pull': pull_time_eu,
            'read_node_store': store_time_eu,
            'read_node_stable': stable_time_eu,
            'read_client_response': read_time_eu,
        }, ignore_index=True)

        pd_result_us = pd_result_us.append({
            'version': client_response['version'],
            'client_request': request_time,
            'write_node_receive': write_node_df.iloc[i]['time'],
            'write_node_respond': write_node_df.iloc[i+1]['time'],
            'client_response': response_time,
            'write_node_push': push_time,
            'read_node_pull': pull_time_us,
            'read_node_store': store_time_us,
            'read_node_stable': stable_time_us,
            'read_client_response': read_time_us,
        }, ignore_index=True)

    pd_result_eu = get_time_diff_cc(
        pd_result_eu.tail(100)).reset_index(drop=True)
    pd_result_eu['consistency'] = 'CC'
    pd_result_eu['delay'] = delay
    pd_result_eu['region'] = LOCAL_REGION

    pd_result_us = get_time_diff_cc(
        pd_result_us.tail(100)).reset_index(drop=True)
    pd_result_us['consistency'] = 'CC'
    pd_result_us['delay'] = delay
    pd_result_us['region'] = REMOTE_REGION

    return pd_result_eu.reset_index(drop=True), pd_result_us.reset_index(drop=True)


def get_push_time(df, version):
    return df[(df['logType'] == 'LOG_PUSH') & (df['version'] >= version)].sort_values('version').iloc[0]['time']


def get_pull_time(df, version):
    return df[(df['logType'] == 'LOG_PULL') & (df['version'] >= version)].sort_values('version').iloc[0]['time']


def get_store_time(df, version):
    return df[(df['logType'] == 'STORE_VERSION') & (df['version'] == version)].iloc[0]['time']


def get_stable_time(df, version):
    return df[(df['logType'] == 'STABLE_TIME') & (df['stableTime'] >= version)].sort_values('stableTime').iloc[0]['time']


def get_read_time_cc(df, version):
    return df[(df['logType'] == 'ROT_RESPONSE') & (df['stableTime'] >= version)].sort_values('stableTime').iloc[0]['time']


def get_read_time_ec(df, id):
    return df[(df['logType'] == 'ROT_RESPONSE') & (df['id'] >= id)].sort_values('id').iloc[0]['time']


def get_time_diff_ec(df_ec):
    df_ec_diff = pd.DataFrame()
    df_ec_diff['response_time'] = get_diff(
        df_ec, 'client_request', 'client_response')
    df_ec_diff['read_time'] = get_diff(
        df_ec, 'client_request', 'read_client_response')
    return df_ec_diff


def get_time_diff_cc(df_cc):
    df_cc_diff = pd.DataFrame()
    df_cc_diff['response_time'] = get_diff(
        df_cc, 'client_request', 'client_response')
    df_cc_diff['push_time'] = get_diff(
        df_cc, 'client_request', 'write_node_push')
    df_cc_diff['pull_time'] = get_diff(
        df_cc, 'client_request', 'read_node_pull')
    df_cc_diff['store_time'] = get_diff(
        df_cc, 'client_request', 'read_node_store')
    df_cc_diff['stable_time'] = get_diff(
        df_cc, 'client_request', 'read_node_stable')
    df_cc_diff['read_time'] = get_diff(
        df_cc, 'client_request', 'read_client_response')
    return df_cc_diff


def visibility_distribution_tables(dfs, delay):
    # Write Latency & Visibility comparison table
    titles = ['Local Write Visibility - Local', 'Remote Write Visibility']
    files = ['local_write_visibility_table', 'remote_write_visibility_table']

    for i in range(0, len(dfs)//2, 1):
        df_ec_stats = df_describe(dfs[i*2], 'read_time')
        df_cc_stats = df_describe(dfs[i*2 + 1], 'read_time')
        visibility_distribution_table(
            df_ec_stats, df_cc_stats, titles[i] + ' (' + str(delay) + ' Inter-Write Delay (ms))', files[i], delay)


def visibility_distribution_table(df_ec_stats, df_cc_stats, title, filename, delay):
    df_ec_stats = df_ec_stats.round(2)
    df_cc_stats = df_cc_stats.round(2)

    df_ec_stats.index = ['EC']
    df_cc_stats.index = ['CC']

    df_result = pd.concat([df_ec_stats, df_cc_stats])
    df_result['consistency'] = df_result.index
    df_result = df_result.reset_index(drop=True)
    df_result = pd.concat(
        [df_result['consistency'], df_result.drop('consistency', axis=1)], axis=1)

    plt.annotate(title, (0.5, 0.7), xycoords='axes fraction',
                 ha='center', va='bottom', fontsize=10)
    plt.table(cellText=df_result.values, colLabels=df_result.columns,
              loc='center', cellLoc='center')
    plt.axis('off')
    plt.savefig(RESULT_PATH + '/' + filename + '_' +
                str(delay) + '.png', dpi=300, bbox_inches='tight')
    plt.clf()


def visibility_boxplot(df, outliers=True):
    g = sns.FacetGrid(df, col="region", col_order=[LOCAL_REGION, REMOTE_REGION], height=10,
                      aspect=0.8, margin_titles=True)
    g.map(sns.boxplot, "delay", "read_time", "consistency", order=DELAYS,
          showfliers=outliers, hue_order=['EC', 'CC'], palette=COLORS)
    g.add_legend()
    g.set_axis_labels("Inter-Write Delay (ms)", "Visibility (ms)")
    g.axes[0][0].set_title("Local Region (EU)")
    g.axes[0][1].set_title("Remote Region (US)")

    plt.savefig(RESULT_PATH + '/visibility_boxplot.png', dpi=300)
    plt.clf()


def write_response_boxplot(df, outliers=True):
    g = sns.FacetGrid(df, col="region", col_order=[LOCAL_REGION, REMOTE_REGION], height=10,
                      aspect=0.8, margin_titles=True)
    g.map(sns.boxplot, "delay", "response_time", "consistency", order=DELAYS,
          showfliers=outliers, hue_order=['EC', 'CC'], palette=COLORS)
    g.add_legend()
    g.set_axis_labels("Inter-Write Delay (ms)", "Write Response Time (ms)")
    g.axes[0][0].set_title("Local Region (EU)")
    g.axes[0][1].set_title("Remote Region (US)")

    plt.savefig(RESULT_PATH + '/write_response_boxplot.png', dpi=300)
    plt.clf()


def stable_time_boxplot(df):
    g = sns.FacetGrid(df, col="region", col_order=[LOCAL_REGION, REMOTE_REGION], height=10,
                      aspect=1, margin_titles=True)
    g.map(sns.boxplot, "delay", "stable_time",
          order=DELAYS, showfliers=False, palette=COLORS)
    g.add_legend()
    g.set_axis_labels("Inter-Write Delay (ms)", "Stable Time (ms)")
    g.axes[0][0].set_title("Local Region (EU)")
    g.axes[0][1].set_title("Remote Region (US)")

    plt.savefig(RESULT_PATH + '/stable_time_boxplot.png', dpi=300)
    plt.clf()


def visibility_barplot(df, scale='linear'):
    grouped_data = df.groupby(["delay", "consistency", "region"])[
        "read_time"]
    average_visibility = grouped_data.mean().reset_index()

    g = sns.FacetGrid(average_visibility, col="region",
                      height=9, aspect=1.5, margin_titles=True)
    g.map(sns.barplot, "delay", "read_time", "consistency",
          order=DELAYS, hue_order=['EC', 'CC'], palette=COLORS, width=0.6, linewidth=1, edgecolor='black', alpha=0.9)
    g.set(yscale=scale)    
    g.add_legend()  
    g.set_axis_labels("Inter-Write Delay (ms)", "Visibility (ms)")
    g.axes[0][0].set_title("Local Region (EU)", pad=30)
    g.axes[0][1].set_title("Remote Region (US)", pad=30)

    # Add y-values on top of each bar
    for ax in g.axes.flat:
        for p in ax.patches:
            ax.annotate(format(p.get_height(), '.2f'),
                        (p.get_x() + p.get_width() / 2., p.get_height()),
                        ha='center', va='center', xytext=(0, 10),
                        textcoords='offset points')

    plt.savefig(RESULT_PATH + '/visibility_barplot_' + scale + '.png', dpi=300)
    plt.clf()


def visibility_throughput_relation(df):
    g = sns.FacetGrid(df, col="region", col_order=[LOCAL_REGION, REMOTE_REGION], height=8, aspect=1, margin_titles=True)
    def estimator_99(data): return np.percentile(data, 99)
    def estimator_95(data): return np.percentile(data, 95)

    for ax, (_, subdata) in zip(g.axes.flat, df.groupby('region')):
        sns.lineplot(data=subdata, x="delay", y="read_time", hue="consistency", style="consistency", markers=MARKERS, dashes=[LINESTYLES[0], LINESTYLES[0]],
                     markersize=6, estimator=estimator_99, errorbar=None, linewidth=3, markeredgewidth=1, markeredgecolor='w', ax=ax, palette=COLORS)
        sns.lineplot(data=subdata, x="delay", y="read_time", hue="consistency", style="consistency", markers=MARKERS, dashes=[LINESTYLES[1], LINESTYLES[1]],
                     markersize=8, estimator=estimator_95, errorbar=None, linewidth=3, markeredgewidth=1, markeredgecolor='w', ax=ax, palette=COLORS)
        sns.lineplot(data=subdata, x="delay", y="read_time", hue="consistency", style="consistency", markers=MARKERS, dashes=[LINESTYLES[2], LINESTYLES[2]],
                     markersize=10, estimator=np.mean, errorbar=None, linewidth=3, markeredgewidth=1, markeredgecolor='w', ax=ax, palette=COLORS)
        ax.xaxis.set_major_formatter(plt.FormatStrFormatter('%.0f'))
        
    g.set_axis_labels("Inter-Write Delay (ms)", "Visibility (ms)")
    g.axes[0][0].set_title("Local Region (EU)")
    g.axes[0][1].set_title("Remote Region (US)")
    
    plt.subplots_adjust(left=0.1, hspace=0.3, wspace=0.1)
    plt.savefig(RESULT_PATH + '/visibility_with_throughput.png', dpi=300)
    plt.clf()

def visibility_throughput_relation_errorbar(df):
    g = sns.FacetGrid(df, col="region", col_order=[LOCAL_REGION, REMOTE_REGION], height=8, aspect=1, margin_titles=True)

    for ax, (_, subdata) in zip(g.axes.flat, df.groupby('region')):
        sns.lineplot(data=subdata, x="delay", y="read_time", hue="consistency", style="consistency", markers=MARKERS, dashes=[LINESTYLES[2], LINESTYLES[2]],
                     markersize=10, estimator=np.mean, linewidth=3, legend=False, markeredgewidth=1, markeredgecolor='w', ax=ax, palette=COLORS)
        ax.xaxis.set_major_formatter(plt.FormatStrFormatter('%.0f'))

    g.set_axis_labels("Inter-Write Delay (ms)", "Visibility (ms)")
    g.axes[0][0].set_title("Local Region (EU)")
    g.axes[0][1].set_title("Remote Region (US)")
    plt.subplots_adjust(left=0.1, hspace=0.3, wspace=0.1)
    plt.savefig(RESULT_PATH + '/visibility_with_throughput_errorbar.png', dpi=300)
    plt.clf()
