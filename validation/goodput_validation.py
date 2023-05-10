import pandas as pd
import matplotlib.pyplot as plt
from utils import get_data
import os

PAYLOAD_BYTES = 12
PATH = os.path.dirname(os.path.abspath(__file__))
LOGS_DIR = PATH + '/logs/goodput'

def goodput_times(iteration_dir, region):
    df = get_data(LOGS_DIR + '/' + iteration_dir, 'readclient-' + region)
    
    df_result = pd.DataFrame()
    df_result['elapsed_time'] = df['time']
    df_result['total_writes'] = df['totalVersions']
    df_result['total_bytes'] = (df_result['total_writes'] * PAYLOAD_BYTES)
    df_result['writes_per_second'] = ((1000 * df_result['total_writes']) / df_result['elapsed_time'])
    df_result['bytes_per_second'] = (df_result['total_bytes'] / df_result['elapsed_time'])

    return df_result

def goodput_stats(df_ev_goodput, df_cc_goodput):
    df_ev_stats = df_ev_goodput.mean().to_frame().transpose()
    df_cc_stats = df_cc_goodput.mean().to_frame().transpose()
    df_ev_stats.index = ['EV']
    df_cc_stats.index = ['CC']

    df_result = pd.concat([df_ev_stats.round(2), df_cc_stats.round(2)])
    df_result['name'] = df_result.index
    df_result = df_result.reset_index(drop=True)

    df_result = pd.concat([df_result['name'], df_result.drop('name', axis=1)], axis=1)

    plt.table(cellText=df_result.values, colLabels=df_result.columns, cellLoc='center', loc='center')
    plt.axis('off')
    plt.annotate('Goodput', (0.5, 0.7), xycoords='axes fraction', ha='center', va='bottom', fontsize=10)
    plt.savefig(PATH + '/results/plots/goodput_table.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.rcParams['figure.figsize'] = plt.rcParamsDefault['figure.figsize']

    
