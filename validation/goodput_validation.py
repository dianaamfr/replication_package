import sys
import pandas as pd
from utils import get_data
import os

PAYLOAD_BYTES = 12
PATH = os.path.dirname(os.path.abspath(__file__))
LOGS_DIR = PATH + '/logs/goodput'

def combine_logs(iteration_dir):
    result_dir = PATH + '/results/goodput/' + iteration_dir
    result_file = result_dir + '/logs.csv'
    if not os.path.exists(result_dir):
        os.makedirs(result_dir)

    dest_file = open(result_file ,'w+', newline='\n', encoding='utf-8')

    df = get_data(LOGS_DIR + '/' + iteration_dir, 'readclient-eu-west-1')

    df_result = pd.DataFrame()
    df_result['total_writes'] = df['totalVersions']
    df_result['total_bytes'] = df_result['total_writes'] * PAYLOAD_BYTES
    df_result['elapsed_time'] = df['time']
    df_result['writes_per_second'] = (1000 * df_result['total_writes']) / df_result['elapsed_time']
    df_result['bytes_per_second'] = df_result['total_bytes'] / df_result['elapsed_time']
    df_result.to_csv(dest_file, index=False)


def get_stats(iteration_dir):
    result_file = PATH + '/results/goodput/' + iteration_dir + '/logs.csv'
    stats_dir = PATH + '/results/goodput/' + iteration_dir
    stats_file = stats_dir + '/validation.csv'
    if not os.path.exists(stats_dir):
        os.makedirs(stats_dir)
    
    df = pd.read_csv(result_file)
    df_results = pd.DataFrame()

    df_results['99%'] = df.quantile(q=0.99)
    df_results['95%'] = df.quantile(q=0.95)
    df_results['50%'] = df.quantile(q=0.50)
    df_results['mean'] = df.mean()
    df_results['max'] = df.max()
    df_results['min'] = df.min()
    
    df_results.to_csv(stats_file)


if __name__ == '__main__':
    combine_logs(sys.argv[1])
    get_stats(sys.argv[1])