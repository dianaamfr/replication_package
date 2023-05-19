import pandas as pd
import os

PATH = os.path.dirname(os.path.abspath(__file__)) + '/../'
CC_DIR = '/causal'
CC_CLIENTS_DIR = '/clients_causal'
EC_DIR = '/eventual'
LOCAL_REGION = 'eu-west-1'
REMOTE_REGION = 'us-east-1'
DELAYS = [50, 100, 200]
GOODPUTS = [1000//delay for delay in DELAYS][::-1]
CLIENTS = [100, 500, 900, 1300, 1700, 2100]
CLIENTS_GOODPUTS = CLIENTS * (1000 // DELAYS[0])
PERCENTILES = [50, 70, 95, 99]
PERCENTILES_FLOAT = [p/100 for p in PERCENTILES]
LINESTYLES = [
    (1, 1),
    (3, 5, 1, 5),
    (5, 5)]
COLORS = ["#006f73", "#da4d30"]
MARKERS = ['X', 'o']


def get_data(path, file):
    return pd.read_json(path + '/' + file + '.json')


def get_diff(df, from_row, to_row):
    return df[to_row] - df[from_row]


def df_describe(df, attr):
    return pd.DataFrame({
        '99%': [df[attr].quantile(q=0.99)],
        '95%': [df[attr].quantile(q=0.95)],
        '70%': [df[attr].quantile(q=0.70)],
        '50%': [df[attr].quantile(q=0.50)],
        'mean': [df[attr].mean()],
        'max': [df[attr].max()],
        'min': [df[attr].min()]})
