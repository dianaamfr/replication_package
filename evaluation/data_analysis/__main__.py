import seaborn as sns
from single_client import latency_evaluation, goodput_evaluation, visibility_evaluation
from multi_client import latency_evaluation as m_latency_evaluation, read_throughout_evaluation, visibility_evaluation as m_visibility_evaluation, write_throughout_evaluation
from utils import PALETTE_FULL, PALETTE_SHORT
import matplotlib.pyplot as plt

sns.set(style="whitegrid", rc={"axes.edgecolor": "black", "axes.linewidth": 1, "grid.linestyle": "--", "grid.linewidth": 0.8})
plt.rcParams['font.family'] = 'serif'

# Single-client data analysis
sns.set_palette(PALETTE_SHORT)

latency_evaluation()
goodput_evaluation()
visibility_evaluation()


# Multi-client data analysis
sns.set_palette(PALETTE_FULL)

m_latency_evaluation()
read_throughout_evaluation()
m_visibility_evaluation()
write_throughout_evaluation()
