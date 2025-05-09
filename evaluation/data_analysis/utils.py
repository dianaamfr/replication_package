import pandas as pd
import os

PATH = os.path.dirname(os.path.abspath(__file__)) + '/..'
CC_DIR = '/causal'
EC_DIR = '/eventual'
LOCAL_REGION = 'eu-west-1'
REMOTE_REGION = 'us-east-1'
DELAYS = [50, 100, 200]
PERCENTILES = [50, 70, 95, 99]
PERCENTILES_FLOAT = [p/100 for p in PERCENTILES]
PALETTE_FULL= ["#0a5c6c", "#008985", "#26b47f", "#93da63", "#ffa600", "#ffe46a"]
PALETTE_SHORT = ["#0a5c6c", "#26b47f", "#93da63"]
MARKERS = ['X', 'o']
PAYLOAD_BYTES = 12


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
