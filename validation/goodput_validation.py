import pandas as pd
from utils import get_data
import os

PATH = os.path.dirname(os.path.abspath(__file__))
LOGS_DIR = PATH + '/logs/goodput'
LOG_RESULT_FILE = PATH + '/results/goodput/logs.csv'
LOG_STATS_FILE = PATH + '/results/goodput/validation.csv'

def get_region_stats(df, region):
    df_result = pd.DataFrame()
    start_time = df.iloc[0]['time']
    df_result['total_bytes'] = df['totalBytes']
    df_result['elapsed_time'] = df['time'] - start_time
    df_result['bytes_per_second'] = (df_result['total_bytes'] * 1000) / df_result['elapsed_time']
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
    print(df.groupby('region')['bytes_per_second'].describe())
    #df.to_csv(LOG_STATS_FILE)


if __name__ == '__main__':
    combine_logs()
    get_stats()