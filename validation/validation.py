import sys
import matplotlib.pyplot as plt
from latency_validation import *
import os

PATH = os.path.dirname(os.path.abspath(__file__))

def latency_validation(path_ev, path_cc):
    df_ev_latency = latency_times(path_ev, 'eu-west-1')
    df_cc_latency = latency_times(path_cc, 'eu-west-1')

    # Latency distribution boxplot
    latency_boxplot(df_ev_latency, df_cc_latency)

    # Latency distribution table
    latency_stats(df_ev_latency, df_cc_latency)

    # Latency histogram
    latency_histogram(df_ev_latency, df_cc_latency)

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('Usage: python3 validation.py <image-tag> <test-id>')
        exit(1)

    path_ev = sys.argv[1] + '/eventual/' + sys.argv[2]
    path_cc = sys.argv[1] + '/causal/' + sys.argv[2]

    # goodput_validation(sys.argv[1], sys.argv[2])
    latency_validation(path_ev, path_cc)
    # visibility_validation(path)
    