import os
from data_analysis.utils import PATH, LOCAL_REGION, MARKERS, PALETTE_FULL
from data_analysis.utils import get_data
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import re

RAW_PATH = PATH + '/logs/multi_client/write_throughput'
RESULTS_PATH  = PATH + '/results/multi_client/write_throughput'
WRITE_TIME = 30

def write_throughout_evaluation():
    node_dirs = sorted([name for name in os.listdir(RAW_PATH) if os.path.isdir(os.path.join(RAW_PATH, name))])
    _, ax = plt.subplots(figsize=(10, 5))

    for i, node_dir in enumerate(node_dirs):
        node_dir = os.path.join(RAW_PATH, node_dir)
        total_writes = sorted(get_node_writes(node_dir), key=lambda x: x[1])
        
        y_coords = [t[0] for t in total_writes]
        x_coords = [t[1] for t in total_writes]
        plt.plot(x_coords, y_coords, marker=MARKERS[1], markersize=9, linewidth=2, linestyle='-', color=PALETTE_FULL[i], markeredgewidth=1, markeredgecolor='w')
    
    ax.xaxis.grid(True)
    ax.set_ylabel("Write Throughput (1000 x ROT/s)", labelpad=10)
    ax.set_xlabel("Client Writing Threads", labelpad=10)
    ax.yaxis.set_major_formatter(ticker.FuncFormatter(lambda y, pos: '{:.0f}'.format(y/1000)))
    ax.legend(['1', '2', '4'], loc='upper left', title = 'Partitions')
    plt.savefig(RESULTS_PATH + '/write_throughput_partitions.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()


def get_node_writes(path):
    write_threads_dirs = [name for name in os.listdir(path) if os.path.isdir(os.path.join(path, name))]
    node_reads = [] 
    for write_threads_dir in write_threads_dirs:
        write_threads = int(re.findall(r"w(\d+)", write_threads_dir)[0])
        write_threads_dir = os.path.join(path, write_threads_dir)
        total_writes = get_iteration_writes(write_threads_dir)
        node_reads.append((total_writes, write_threads))
    return node_reads


def get_iteration_writes(path):
    df = get_data(path, 'writeclient-' + LOCAL_REGION)
    return (df.loc[df['logType'] == 'THROUGHPUT', 'total'].values[0] / WRITE_TIME).round(2)
