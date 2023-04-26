import json
import pandas as pd

def get_data(path, file):
    return pd.read_json(path + '/' + file + '.json')

def get_file(path, file):
    return json.loads(open(path + '/' + file + '.json', 'r').read())

def get_diff(df, from_row, to_row):
    return df[to_row] - df[from_row]