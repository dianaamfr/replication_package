import os
import re
import json
import pandas as pd
import datetime
import argparse

PATH = os.path.dirname(os.path.abspath(__file__)) + '/logs'
TIMESTAMP_FORMAT = "{:020d}{}{:020d}"
TIMESTAMP_SEPARATOR = "-"
MIN_TIMESTAMP = TIMESTAMP_FORMAT.format(0, TIMESTAMP_SEPARATOR, 0)
NUM_PARTITIONS = 2

# Utils
def get_key_partition(key):
    hash_code = 0
    for char in key:
        hash_code = (31 * hash_code + ord(char)) & 0xFFFFFFFF
    return hash_code % NUM_PARTITIONS + 1


# Parse
def get_file_data(path):
    with open(path, 'r') as file:
        return file.read()

def get_log_limits():
    partitions = [name for name in os.listdir(PATH) if os.path.isdir(os.path.join(PATH, name))]
    max_log_versions = []
    min_log_versions = []

    for partition in partitions:
        partition_id = re.findall(r'p(\d+)', partition)[0]
        partition_dir = os.path.join(PATH, partition)
        log_versions = [name for name in os.listdir(partition_dir) if os.path.isfile(os.path.join(partition_dir, name))]
        log_versions.sort()
        max_log_versions.append(log_versions[-1])
        min_log_versions.append(log_versions[0])
    return max(max_log_versions), min(min_log_versions)

def parse_log(partition, timestamp):
    df = pd.DataFrame(columns=['key', 'value', 'timestamp'])

    partition_dir = os.path.join(PATH, 'p' + str(partition))
    log_versions = [name for name in os.listdir(partition_dir) if os.path.isfile(os.path.join(partition_dir, name)) and (name >= timestamp)]

    if not log_versions:
        return df
    
    log_version = min(log_versions)
    log_path = os.path.join(partition_dir, log_version)
    data = json.loads(get_file_data((log_path)))
    rows = [{'key': state['key'], 'value': version['value'], 'timestamp': version['timestamp']}
        for state in data['state']
        for version in state['versions']]
    df = df.append(rows, ignore_index=True)

    df['log_version'] = log_version
    return df

def parse_logs(partition, timestamp):
    df = pd.DataFrame(columns=['key', 'value', 'timestamp'])

    partition_dir = os.path.join(PATH, 'p' + str(partition))
    log_versions = [name for name in os.listdir(partition_dir) if os.path.isfile(os.path.join(partition_dir, name)) and (name >= timestamp)]

    if not log_versions:
        return df
    
    for log_version in log_versions:
        log_path = os.path.join(partition_dir, log_version)
        data = json.loads(get_file_data((log_path)))
        rows = [{'key': state['key'], 'value': version['value'], 'timestamp': version['timestamp']}
            for state in data['state']
            for version in state['versions']]
        df = df.append(rows, ignore_index=True)
        df['log_version'] = log_version

    df = df.groupby(['key', 'value', 'timestamp']).apply(lambda x: x.sort_values('log_version').head(1)).reset_index(drop=True)
    return df

def get_date(timestamp):
    l = re.findall(r'(\d+)-', timestamp)[0]
    return datetime.datetime.fromtimestamp(int(l) / 1000)

def get_timestamp(date):
    try:
        date = datetime.datetime.strptime(date, "%Y-%m-%d %H:%M:%S.%f")
    except ValueError:
        print("Invalid date and time format. Please enter a date and time in YYYY-MM-DD HH:MM:SS.sss format.")
        exit(1)
    return TIMESTAMP_FORMAT.format(int(date.timestamp() * 1000), TIMESTAMP_SEPARATOR, 0)


# Options
def get_value(keys, timestamp):
    keys = [value.strip() for value in keys.split(',') if value.strip()]
    if not keys:
        print("Invalid keys. Please enter at least one key.")
        exit(1)
    
    logs_dfs = []
    for key in keys:
        partition = get_key_partition(key) 
        contains_partition = any(log_df[0] == partition for log_df in logs_dfs)

        log_df = None
        if not contains_partition:
            log_df = parse_log(partition, timestamp)
            logs_dfs.append((partition, log_df))
        else:
            log_df = [log_df[1] for log_df in logs_dfs if log_df[0] == partition][0]

        result = log_df[((log_df['key'] == key) & (log_df['timestamp'] <= timestamp))].sort_values(by=['timestamp'], ascending=False)
        
        print("Key = " + key)
        if result.empty:
            print(" > value = None")
        else:
            version = result.iloc[0]
            print(" > value = " + version['value'])
            print(" > timestamp = " + str(version['timestamp']))
            print(" > log version = " + version['log_version'])
            print(" > partition = " + str(partition))
        print()

def get_history(key, timestamp, checkpointing=False):
    partition = get_key_partition(key)
    log_df = parse_logs(partition, timestamp) if checkpointing else parse_log(partition, timestamp)

    versions = log_df[((log_df['key'] == key) & (log_df['timestamp'] <= timestamp))][['timestamp', 'value']].drop_duplicates().sort_values('timestamp').reset_index(drop=True)
    print("History of key " + key + ":")
    if versions.empty:
        print(" > No versions available")
    else:
        print(versions.to_string(justify='left'))


# Process args
def process_args(args):
    if args.date:
        timestamp = get_timestamp(args.date)
        if args.history:
            get_history(args.keys, timestamp, args.checkpointing)
        else:
            get_value(args.keys, timestamp)
    elif args.timestamp:
        if args.history:
            get_history(args.keys, args.timestamp, args.checkpointing)
        else:
            get_value(args.keys, args.timestamp)

def set_parser(parser):
    group = parser.add_mutually_exclusive_group()
    group.add_argument('-d', '--date', help='Date in YYYY-MM-DD HH:MM:SS.sss format')
    group.add_argument('-t', '--timestamp', help='Timestamp')

    parser.add_argument('-i', '--info', help='Show info about log file', action='store_true', default=False)
    parser.add_argument('-c', '--checkpointing', help='Indicates that checkpointing was active', action='store_true')

    group2 = parser.add_mutually_exclusive_group()
    group2.add_argument('-hist', '--history', help='Show history of key at date or timestamp', action='store_true')
    group2.add_argument('-v', '--version', help='Show version of key at date or timestamp', action='store_true')
   
    parser.add_argument('-k', '--keys', help='Keys to search for, separated by commas')


if __name__ == "__main__":
    # Set up parser
    parser = argparse.ArgumentParser()
    set_parser(parser)
    args = parser.parse_args()

    # Show info about log file
    if args.info:
        max_timestamp, min_timestamp = get_log_limits()
        min_date = get_date(min_timestamp)
        max_date = get_date(max_timestamp)
        print("Min log time: {}  /  {}".format(min_date, min_timestamp))
        print("Max log time: {}  /  {}".format(max_date, max_timestamp))

    # Process args
    if (not args.keys) and (args.history or args.version or args.date or args.timestamp):
        parser.error("Must specify keys to search for")

    if (args.history or args.version) and not (args.date or args.timestamp):
        parser.error("Must specify date or timestamp")

    process_args(args)