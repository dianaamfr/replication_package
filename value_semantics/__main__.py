import os
import re
import json
import pandas as pd
import datetime

PATH = os.path.dirname(os.path.abspath(__file__)) + '/logs'
TIMESTAMP_FORMAT = "{:020d}{}{:020d}".format(0, "-", 0)
MIN_TIMESTAMP = TIMESTAMP_FORMAT

def get_file_data(path):
    with open(path, 'r') as file:
        return file.read()

def parse_logs():
    dfs = []
    max_log_version = MIN_TIMESTAMP
    min_log_version = None

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
            df['log_version_date'] = get_date(log_version)
            df['timestamp_date'] = df['timestamp'].apply(get_date)
            df['partition'] = partition_id
            dfs.append(df)
        max_log_version = max(max_log_version, log_versions[-1])
        min_log_version = min(min_log_version, log_versions[0]) if min_log_version else log_versions[0]
    return pd.concat(dfs), max_log_version, min_log_version

def get_date(timestamp):
    l = re.findall(r'(\d+)-', timestamp)[0]
    return datetime.datetime.fromtimestamp(int(l) / 1000)

def get_value_input():
    while True:
        date_str = input("Enter date & time (YYYY-MM-DD HH:MM:SS.sss): ")
        keys_str = input("Enter keys (e.g. a,b,c) or nothing to see all keys): ")
        print()
        try:
            date = datetime.datetime.strptime(date_str, "%Y-%m-%d %H:%M:%S.%f")
            keys = [value.strip() for value in keys_str.split(',') if value.strip()]
            return date, keys
        except ValueError:
            print("Invalid date and time format. Please enter a date and time in YYYY-MM-DD HH:MM:SS.sss format.")

def get_history_input():
    while True:
        date_str = input("Enter date & time (YYYY-MM-DD HH:MM:SS.sss): ")
        key = input("Enter key: ")
        try:
            date = datetime.datetime.strptime(date_str, "%Y-%m-%d %H:%M:%S.%f")
            return date, key
        except ValueError:
            print("Invalid date and time format. Please enter a date and time in YYYY-MM-DD HH:MM:SS.sss format.")

def get_value(df, default_keys):
    date, keys = get_value_input()
    if not keys:
        keys = default_keys
    for key in keys:
        result = df[((df['log_version_date'] <= date) & (df['key'] == key) & (df['timestamp_date'] <= date))].sort_values(by=['log_version', 'timestamp'], ascending=False)
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

def get_history(df):
    date, key = get_history_input()
    versions = df[((df['key'] == key) & (df['timestamp_date'] <= date))][['timestamp', 'value']].drop_duplicates().sort_values('timestamp').reset_index(drop=True)
    styler = versions.style.set_table_styles([{'selector': 'th', 'props': [('text-align', 'left')]}])
    print("History of key " + key + ":")
    if versions.empty:
        print(" > No versions available")
    else:
        print(styler)

def menu(df, keys):
    print("Select an option:")
    while True:
        print("1. Key at datetime")
        print("2. Key history at datetime")
        # print("3. Key at timestamp")
        # print("4. Key history at timestamp")
        print("3. Exit")
        option = input("Enter option: ")
        print()

        if option == "1":
            get_value(df, keys)
        elif option == "2":
            get_history(df)
        elif option == "3":
            return
        print()

if __name__ == "__main__":
    df, max_log_version, min_log_version = parse_logs()

    min_date = get_date(min_log_version)
    max_date = get_date(max_log_version)
    keys = df['key'].unique()
    print("Min date: {}".format(min_date))
    print("Max date: {}".format(max_date))
    print("Keys: {}\n".format(keys))

    menu(df, keys)