import pandas as pd
from utils import get_data
import os

PAYLOAD_BYTES = 12
PATH = os.path.dirname(os.path.abspath(__file__))
LOGS_DIR = PATH + '/logs/goodput'
LOG_RESULT_FILE = PATH + '/results/goodput/logs.csv'
LOG_STATS_FILE = PATH + '/results/goodput/validation.csv'

def get_region_stats(df, region):
    df_result = pd.DataFrame()
    df_result['total_writes'] = df['totalWrites']
    df_result['total_bytes'] = df_result['total_writes'] * PAYLOAD_BYTES
    df_result['elapsed_time'] = df['time']
    df_result['writes_per_second'] = (df_result['total_writes'] * PAYLOAD_BYTES) / df_result['elapsed_time']
    df_result['bytes_per_second'] = df_result['total_bytes'] / df_result['elapsed_time']
    df_result['region'] = region
    return df_result

def combine_logs():
    dest_file = open(LOG_RESULT_FILE ,'w+', newline='\n', encoding='utf-8')
  
    df_us = get_data(LOGS_DIR, 'readclient-us-east-1')
    df_eu = get_data(LOGS_DIR, 'readclient-eu-west-1')

    df_result_us = get_region_stats(df_us, 'us-east-1')
    df_result_eu = get_region_stats(df_eu, 'eu-west-1')

    df_concat = pd.concat([df_result_eu, df_result_us], axis=0).reset_index(drop=True)
    df_concat.to_csv(dest_file, index=False)


def get_stats():
    df = pd.read_csv(LOG_RESULT_FILE)
    df_results = df.groupby('region')['bytes_per_second']

    df_results['99%'] = df.quantile(q=0.99)
    df_results['95%'] = df.quantile(q=0.95)
    df_results['50%'] = df.quantile(q=0.50)
    df_results['mean'] = df.mean()
    df_results['max'] = df.max()
    df_results['min'] = df.min()
    
    df_results.to_csv(LOG_STATS_FILE)


if __name__ == '__main__':
    combine_logs()
    get_stats()