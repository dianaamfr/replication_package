import pandas as pd
import matplotlib.pyplot as plt
from utils import get_data
import os

PATH = os.path.dirname(os.path.abspath(__file__))
LOGS_DIR = PATH + '/logs/latency'

def latency_times(iteration_dir, region):
    df = get_data(LOGS_DIR + '/' + iteration_dir, 'readclient-' + region)
    
    df_result = df.groupby('id').apply(lambda group: 
        group.loc[group['logType'] == 'ROT_RESPONSE', 'time'].values[0] - 
        group.loc[group['logType'] == 'ROT_REQUEST', 'time'].values[0]).reset_index(name='latency')

    return df_result.tail(100).reset_index(drop=True)

def latency_boxplot(df_ev_latency, df_cc_latency):
    percentiles = [50, 70, 95, 99]
    latency_ev_summary = df_ev_latency['latency'].describe(percentiles=[p/100 for p in percentiles])
    latency_cc_summary = df_cc_latency['latency'].describe(percentiles=[p/100 for p in percentiles])

    # Latency distribution boxplot
    fig, axs = plt.subplots(nrows=1, ncols=2, figsize=(10, 7))

    max_latency = max(max(df_ev_latency['latency']), max(df_cc_latency['latency'])) + 5
    axs[0].set_yticks(range(0, max_latency, 5))
    axs[1].set_yticks(range(0, max_latency, 5))

    df_ev_latency.plot.box(y='latency', ax=axs[0], grid=True)
    axs[0].set_title('Eventually Consistent Baseline EU')

    for p in percentiles:
        axs[0].axhline(latency_ev_summary[f'{p}%'], linestyle='--', color='green', label=f'{p}%')
        axs[1].axhline(latency_cc_summary[f'{p}%'], linestyle='--', color='green', label=f'{p}%')

    axs[0].axhline(latency_ev_summary['mean'], linestyle='-', color='blue', label='mean')
    axs[0].axhline(latency_ev_summary['min'], linestyle=':', color='gray', label='min')
    axs[0].axhline(latency_ev_summary['max'], linestyle=':', color='gray', label='max')
    axs[0].legend()
    axs[0].set_ylabel('Latency (ms)')

    axs[1].axhline(latency_ev_summary['mean'], linestyle='-', color='blue', label='mean')
    axs[1].axhline(latency_ev_summary['min'], linestyle=':', color='gray', label='min')
    axs[1].axhline(latency_ev_summary['max'], linestyle=':', color='gray', label='max')
    axs[1].legend()
    axs[1].set_ylabel('Latency (ms)')

    df_cc_latency.plot.box(y='latency', ax=axs[1], grid=True)
    axs[1].set_title('Causally Consistent Prototype EU')

    fig.suptitle('Latency Distributions')

    plt.tight_layout()    
    plt.savefig(PATH + '/results/plots/latency_boxplot.png')
    plt.clf()


def latency_stats(df_ev_latency, df_cc_latency): 
    df_ev_stats = latency_table(df_ev_latency)
    df_cc_stats = latency_table(df_cc_latency)
    df_ev_stats.index = ['ev_latency']
    df_cc_stats.index = ['cc_latency']

    df_result = pd.concat([df_ev_stats.round(2), df_cc_stats.round(2)], axis=0)

    plt.table(cellText=df_result.values, colLabels=df_result.columns)
    plt.axis('off')
    plt.savefig(PATH + '/results/plots/latency_table.png', bbox_inches='tight', loc='center')
    plt.clf()

def latency_histogram(df_ev_latency, df_cc_latency):
    plt.figure(figsize=(10, 7))
    plt.hist(df_ev_latency['latency'], alpha=0.5, bins=30, label='EV')
    plt.hist(df_cc_latency['latency'], alpha=0.5, bins=30, label='CC')

    plt.legend(loc='upper right')
    plt.xlabel('Latency (ms)')
    plt.ylabel('Frequency')

    plt.savefig(PATH + '/results/plots/latency_histogram.png')
    plt.clf()

def latency_table(df):
    return pd.DataFrame({
        '99%': [df['latency'].quantile(q=0.99)],
        '95%': [df['latency'].quantile(q=0.95)],
        '70%': [df['latency'].quantile(q=0.70)],
        '50%': [df['latency'].quantile(q=0.50)],
        'mean': [df['latency'].mean()],
        'max': [df['latency'].max()],
        'min': [df['latency'].min()]})

