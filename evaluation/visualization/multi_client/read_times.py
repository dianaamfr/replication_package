import os
from visualization.utils import PATH, LOCAL_REGION, MARKERS, COLORS
from visualization.utils import get_data
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import math
import re

RAW_DIR = PATH + 'logs/scale/read_times'
RESULTS_DIR = PATH + '/results/read_times'
print(PATH)
READ_TIME = 30

def read_throughout_evaluation():
    node_dirs = sorted([name for name in os.listdir(RAW_DIR) if os.path.isdir(os.path.join(RAW_DIR, name))])
    _, ax = plt.subplots(figsize=(7, 5))

    for i, node_dir in enumerate(node_dirs):
        node_dir = os.path.join(RAW_DIR, node_dir)
        total_reads = sorted(get_node_reads(node_dir), key=lambda x: x[1])
        
        y_coords = [t[0] for t in total_reads]
        x_coords = [t[1] for t in total_reads]
        plt.plot(x_coords, y_coords, marker=MARKERS[1], markersize=9, linewidth=2, linestyle='-', color=COLORS[i], markeredgewidth=1, markeredgecolor='w')
    
    ax.xaxis.grid(True)
    ax.set_ylabel(" (1000 x ROT/s)", labelpad=10)
    ax.set_xlabel("Read Threads", labelpad=10)
    ax.yaxis.set_major_formatter(ticker.FuncFormatter(lambda y, pos: '{:.0f}'.format(y/1000)))
    ax.legend(['1', '2', '3', '4'], loc='upper left', title = 'Read Nodes')
    plt.savefig(RESULTS_DIR + '/read_throughput.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()
        
def get_node_reads(path):
    read_threads_dirs = [name for name in os.listdir(path) if os.path.isdir(os.path.join(path, name))]
    node_reads = [] 
    for read_threads_dir in read_threads_dirs:
        read_threads = int(re.findall(r"r(\d+)", read_threads_dir)[0])
        read_threads_dir = os.path.join(path, read_threads_dir)
        total_reads = get_iteration_reads(read_threads_dir)
        node_reads.append((total_reads, read_threads))
    return node_reads

def get_iteration_reads(path):
    df = get_data(path, 'readclient-' + LOCAL_REGION)
    return (df.loc[df['logType'] == 'ROT_COUNT', 'totalReads'].values[0] / READ_TIME).round(2)
