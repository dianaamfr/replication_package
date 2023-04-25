import sys
import pandas as pd
from utils import get_data
import os

PAYLOAD_BYTES = 12
PATH = os.path.dirname(os.path.abspath(__file__))
LOGS_DIR = PATH + '/logs/goodput'
LOG_RESULT_FILE = PATH + '/results/goodput/logs.csv'
LOG_STATS_FILE = PATH + '/results/goodput/validation.csv'

def combine_logs(iteration_dir):
    dest_file = open(LOG_RESULT_FILE ,'w+', newline='\n', encoding='utf-8')

    df = get_data(LOGS_DIR + '/' + iteration_dir, 'readclient-eu-west-1')

    df_result = pd.DataFrame()
    df_result['total_writes'] = df['totalVersions']
    df_result['total_bytes'] = df_result['total_writes'] * PAYLOAD_BYTES
    df_result['elapsed_time'] = df['time']
    df_result['writes_per_second'] = (1000 * df_result['total_writes']) / df_result['elapsed_time']
    df_result['bytes_per_second'] = df_result['total_bytes'] / df_result['elapsed_time']
    df_result.to_csv(dest_file, index=False)


def get_stats():
    df = pd.read_csv(LOG_RESULT_FILE)
    df_results = pd.DataFrame()

    df_results['99%'] = df.quantile(q=0.99)
    df_results['95%'] = df.quantile(q=0.95)
    df_results['50%'] = df.quantile(q=0.50)
    df_results['mean'] = df.mean()
    df_results['max'] = df.max()
    df_results['min'] = df.min()
    
    df_results.to_csv(LOG_STATS_FILE)


if __name__ == '__main__':
    combine_logs(sys.argv[1])
    get_stats()