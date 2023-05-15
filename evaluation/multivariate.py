import sys
from evaluation.latency import latency_throughput_relation
from evaluation.visibility import visibility_throughput_relation
from evaluation.goodput import goodput_latency_relation

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('Usage: python3 evaluation.py <image-tag>')
        exit(1)

    path_ev = sys.argv[1] + '/eventual/'
    path_cc = sys.argv[1] + '/causal/'

    latency_throughput_relation(path_ev, path_cc)
    goodput_latency_relation(path_ev, path_cc)
    visibility_throughput_relation(path_ev, path_cc)
    