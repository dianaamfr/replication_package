import pandas as pd
from utils import get_data
import os

PATH = os.path.dirname(os.path.abspath(__file__))
LOGS_DIR = PATH + '/logs/goodput'
LOG_RESULT_FILE = PATH + '/results/goodput/logs.csv'
LOG_STATS_FILE = PATH + '/results/goodput/validation.csv'

def get_header():
    return [
        'elapsed_time',
        'versions',
        'bytes'
        ]

def combine_logs():
    dest_file = open(LOG_RESULT_FILE ,'w+', newline='\n', encoding='utf-8')
  
    # Data frame
    df = get_data(LOGS_DIR, 'writeclient-us-east-1')

    start_time = df.iloc[0]['time']
    df = df.rename(columns={'totalBytes': 'total_bytes'})
    df['elapsed_time'] = df['time'] - start_time
    df['bytes_per_second'] = (df['total_bytes'] * 1000) / df['elapsed_time']
    df[['elapsed_time', 'total_bytes', 'bytes_per_second']].to_csv(dest_file, index=False)
    

def get_stats():
    df = pd.read_csv(LOG_RESULT_FILE)
    print(df.describe())
    df.to_csv(LOG_STATS_FILE)


if __name__ == '__main__':
    combine_logs()
    get_stats()