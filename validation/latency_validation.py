import sys
import pandas as pd
from utils import get_data
import os

PATH = os.path.dirname(os.path.abspath(__file__))
LOGS_DIR = PATH + '/logs/latency'

def combine_logs(iteration_dir):
    result_dir = PATH + '/results/latency/' + iteration_dir
    result_file = result_dir + '/logs.csv'
    if not os.path.exists(result_dir):
        os.makedirs(result_dir)

    dest_file = open(result_file ,'w+', newline='\n', encoding='utf-8')

    df_us = get_data(LOGS_DIR + '/' + iteration_dir, 'readclient-us-east-1')
    df_eu = get_data(LOGS_DIR + '/' + iteration_dir, 'readclient-eu-west-1')
    
    df_result_us = df_us.groupby('id').apply(lambda group: 
        group.loc[group['logType'] == 'ROT_RESPONSE', 'time'].values[0] - 
        group.loc[group['logType'] == 'ROT_REQUEST', 'time'].values[0]).reset_index(name='latency')
    df_result_us['region'] = 'us-east-1'

    df_result_eu = df_eu.groupby('id').apply(lambda group: 
        group.loc[group['logType'] == 'ROT_RESPONSE', 'time'].values[0] - 
        group.loc[group['logType'] == 'ROT_REQUEST', 'time'].values[0]).reset_index(name='latency')
    df_result_eu['region'] = 'eu-west-1'

    df_concat = pd.concat([df_result_eu, df_result_us], axis=0).reset_index(drop=True)
    df_concat.to_csv(dest_file, index=False)


def get_stats(iteration_dir):
    result_file = PATH + '/results/latency/' + iteration_dir + '/logs.csv'
    stats_dir = PATH + '/results/latency/' + iteration_dir
    stats_file = stats_dir + '/validation.csv'
    if not os.path.exists(stats_dir):
        os.makedirs(stats_dir)
    
    df = pd.read_csv(result_file).groupby('region')['latency']
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