import sys
from latency_validation import latency_times, latency_stats, latency_boxplot, latency_histogram
from goodput_validation import goodput_times, goodput_stats
from visibility_validation import ev_visibility_times, cc_visibility_times, get_time_diff_ev, get_time_diff_cc, \
    visibility_tables, visibility_boxplot, stable_time_boxplot, visibility_histogram, visibility_histogram_cc
import os

PATH = os.path.dirname(os.path.abspath(__file__))

def latency_validation(path_ev, path_cc):
    df_ev_latency = latency_times(path_ev, 'eu-west-1')
    df_cc_latency = latency_times(path_cc, 'eu-west-1')

    # Latency distribution table
    latency_stats(df_ev_latency, df_cc_latency)

    # Latency distribution boxplot
    latency_boxplot(df_ev_latency, df_cc_latency)

    # Latency histogram
    latency_histogram(df_ev_latency, df_cc_latency)

def goodput_validation(path_ev, path_cc):
    df_ev_goodput = goodput_times(path_ev, 'eu-west-1')
    df_cc_goodput= goodput_times(path_cc, 'eu-west-1')

    goodput_stats(df_ev_goodput, df_cc_goodput)

def visibility_validation(path_ev, path_cc):
    df_ev_eu, df_ev_us = ev_visibility_times(path_ev)
    df_cc_eu, df_cc_us = cc_visibility_times(path_cc)

    df_ev_diff_eu = get_time_diff_ev(df_ev_eu)
    df_cc_diff_eu = get_time_diff_cc(df_cc_eu)
    df_ev_diff_us = get_time_diff_ev(df_ev_us)
    df_cc_diff_us = get_time_diff_cc(df_cc_us) 

    visibility_tables(df_ev_diff_eu, df_cc_diff_eu, df_ev_diff_us, df_cc_diff_us)

    # Boxplot with the time it takes for a write to be visible to a read
    visibility_boxplot(df_ev_diff_eu, df_cc_diff_eu, df_ev_diff_us, df_cc_diff_us)

    # Boxplotwith the time it takes for a write to be stable
    stable_time_boxplot(df_cc_diff_eu,df_cc_diff_us)

    # TODO: Duration of each phase


    # Distribution of the visibility times (comparison between eu and west for eventual and causal)
    visibility_histogram(df_ev_diff_eu, df_cc_diff_eu, df_ev_diff_us, df_cc_diff_us)

    # Distribution of the visibility times of each phase
    visibility_histogram_cc(df_cc_diff_eu, df_cc_diff_us)

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('Usage: python3 validation.py <image-tag> <test-id>')
        exit(1)

    path_ev = sys.argv[1] + '/eventual/' + sys.argv[2]
    path_cc = sys.argv[1] + '/causal/' + sys.argv[2]

    latency_validation(path_ev, path_cc)
    goodput_validation(path_ev, path_cc)
    visibility_validation(path_ev, path_cc)
    