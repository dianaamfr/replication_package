import os
import re
import json
import pandas as pd

PATH = os.path.dirname(os.path.abspath(__file__)) + '/logs'

def get_file_data(path):
    with open(path, 'r') as file:
        return file.read()

def get_dfs():
    dfs = []
    partitions = [name for name in os.listdir(PATH) if os.path.isdir(os.path.join(PATH, name))]
    for partition in partitions:
        partition_dfs = []
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
            print(df)
            partition_dfs.append((log_version, df))
        dfs.append((partition, partition_dfs))
    return dfs


if __name__ == "__main__":
    dfs = get_dfs()
