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
            df['log_version'] = log_version
            df['partition'] = partition_id
            dfs.append(df)
        max_log_version = max(max_log_version, log_versions[-1])
        min_log_version = min(min_log_version, log_versions[0]) if min_log_version else log_versions[0]
    return pd.concat(dfs), max_log_version, min_log_version

def get_date(timestamp):
    l = re.findall(r'(\d+)-', timestamp)[0]
    return datetime.datetime.fromtimestamp(int(l) / 1000)

if __name__ == "__main__":
    df, max_log_version, min_log_version = parse_logs()

    max_date = get_date(max_log_version)
    min_date = get_date(min_log_version)

    # print(df)
    print(max_date)
    print(min_date)
