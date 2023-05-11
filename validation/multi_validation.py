import sys
from latency_validation import latency_times, latency_throughput_relation

def latency_validation(path_ev, path_cc):
    latency_throughput_relation(path_ev, path_cc)

    pass

def goodput_validation(path_ev, path_cc):
    pass

def visibility_validation(path_ev, path_cc):
    pass

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('Usage: python3 validation.py <image-tag>')
        exit(1)

    path_ev = sys.argv[1] + '/eventual/'
    path_cc = sys.argv[1] + '/causal/'

    latency_validation(path_ev, path_cc)
    goodput_validation(path_ev, path_cc)
    visibility_validation(path_ev, path_cc)
    