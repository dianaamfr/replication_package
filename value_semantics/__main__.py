import os
import re
import json
import pandas as pd
import datetime

PATH = os.path.dirname(os.path.abspath(__file__)) + '/logs'
TIMESTAMP_FORMAT = "{:020d}{}{:020d}".format(0, "-", 0)
MIN_TIMESTAMP = TIMESTAMP_FORMAT

# Parse
def get_file_data(path):
    with open(path, 'r') as file:
        return file.read()

def parse_logs():
    dfs = []

    partitions = [name for name in os.listdir(PATH) if os.path.isdir(os.path.join(PATH, name))]
    for partition in partitions:
        partition_id = re.findall(r'p(\d+)', partition)[0]
        partition_dir = os.path.join(PATH, partition)

        log_versions = [name for name in os.listdir(partition_dir) if os.path.isfile(os.path.join(partition_dir, name))]
        log_versions.sort()
        for log_version in log_versions:
            log_path = os.path.join(partition_dir, log_version)
            data = json.loads(get_file_data((log_path)))
            rows = [{'key': state['key'], 'value': version['value'], 'timestamp': version['timestamp']}
                    for state in data['state']
                    for version in state['versions']]
            df = pd.DataFrame(rows)
            if(df.empty):
                continue
            df['log_version'] = log_version
            df['partition'] = partition_id
            dfs.append(df)

    df = pd.concat(dfs)

    # Get max log version seen from all partitions
    max_timestamp = (df.groupby('partition')['log_version'].max()).min()

    # Get min timestamp
    df = df.groupby(['key', 'value', 'timestamp', 'partition']).apply(lambda x: x.sort_values('log_version').head(1)).reset_index(drop=True)
    min_timestamp = df['timestamp'].min()

    df['log_version_date'] = get_date(log_version)
    df['timestamp_date'] = df['timestamp'].apply(get_date)

    return df, max_timestamp, min_timestamp

def get_date(timestamp):
    l = re.findall(r'(\d+)-', timestamp)[0]
    return datetime.datetime.fromtimestamp(int(l) / 1000)


# Get Input
def get_value_by_date_input(max_date):
    while True:
        date_str = input("Enter date & time (YYYY-MM-DD HH:MM:SS.sss): ")
        keys_str = input("Enter keys (e.g. a,b,c) or nothing to see all keys): ")
        print()
        try:
            date = datetime.datetime.strptime(date_str, "%Y-%m-%d %H:%M:%S.%f")
            if date > max_date:
                print("Invalid date. Please enter a date less than or equal to " + str(max_date))
                continue
            keys = [value.strip() for value in keys_str.split(',') if value.strip()]
            return date, keys
        except ValueError:
            print("Invalid date and time format. Please enter a date and time in YYYY-MM-DD HH:MM:SS.sss format.")

def get_value_by_timestamp_input(max_timestamp):
    while True:
        timestamp = input("Enter timestamp: ")
        keys_str = input("Enter keys (e.g. a,b,c) or nothing to see all keys): ")
        print()

        if timestamp > max_timestamp:
            print("Invalid timestamp. Please enter a timestamp less than or equal to " + max_timestamp)
    
        keys = [value.strip() for value in keys_str.split(',') if value.strip()]
        return timestamp, keys

def get_history_by_date_input(max_date):
    while True:
        date_str = input("Enter date & time (YYYY-MM-DD HH:MM:SS.sss): ")
        key = input("Enter key: ")
        print()
        try:
            date = datetime.datetime.strptime(date_str, "%Y-%m-%d %H:%M:%S.%f")
            if date > max_date:
                print("Invalid date. Please enter a date less than or equal to " + str(max_date))
                continue
            return date, key
        except ValueError:
            print("Invalid date and time format. Please enter a date and time in YYYY-MM-DD HH:MM:SS.sss format.")

def get_history_by_timestamp_input(max_timestamp):
    timestamp = max_timestamp
    while True:
        timestamp = input("Enter timestamp: ")
        key = input("Enter key: ")
        print()

        if timestamp > max_timestamp:
            print("Invalid timestamp. Please enter a timestamp less than or equal to " + max_timestamp)
        else:
            return timestamp, key


# Options
def get_value(df, default_keys, max_time, is_timestamp=False):
    date, keys = get_value_by_timestamp_input(max_time) if is_timestamp else get_value_by_date_input(max_time)
    timestamp_col = 'timestamp' if is_timestamp else 'timestamp_date'
    if not keys:
        keys = default_keys
    for key in keys:
        result = df[((df['key'] == key) & (df[timestamp_col] <= date))].sort_values(by=['log_version', 'timestamp'], ascending=False)
        print("Key = " + key, end=", ")
        if result.empty:
            print("Log version = None")
            print(" > value = None")
        else:
            version = result.iloc[0]
            print("Log version = " + version['log_version'])
            print(" > value = " + version['value'])
            print(" > timestamp = " + str(version['timestamp']))
            print(" > date = " + str(version['timestamp_date']))
        print()

def get_history(df, max_time, is_timestamp=False):
    date, key = get_history_by_timestamp_input(max_time) if is_timestamp else get_history_by_date_input(max_time)
    col= 'timestamp' if is_timestamp else 'timestamp_date'
    versions = df[((df['key'] == key) & (df[col] <= date))][['timestamp', 'value']].drop_duplicates().sort_values('timestamp').reset_index(drop=True)
    print("History of key " + key + ":")
    if versions.empty:
        print(" > No versions available")
    else:
        print(versions.to_string(justify='left'))

def menu(df, keys, max_timestamp, max_date):
    print("Select an option:")
    while True:
        print("1. Key at datetime")
        print("2. Key history at datetime")
        print("3. Key at timestamp")
        print("4. Key history at timestamp")
        print("5. Exit")
        option = input("Enter option: ")
        print()

        if option == "1":
            get_value(df, keys, max_date)
        elif option == "2":
            get_history(df, max_date)
        elif option == "3":
            get_value(df, keys, max_timestamp, True)
        elif option == "4":
            get_history(df, max_timestamp, True)
        elif option == "5":
            return
        print()

if __name__ == "__main__":
    df, max_timestamp, min_timestamp = parse_logs()

    min_date = get_date(min_timestamp)
    max_date = get_date(max_timestamp)
    keys = df['key'].unique()
    print("Min time: {}  /  {}".format(min_date, min_timestamp))
    print("Max time: {}  /  {}".format(max_date, max_timestamp))
    print("Keys: {}\n".format(keys))

    menu(df, keys, max_timestamp, max_date)