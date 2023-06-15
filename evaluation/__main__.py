import seaborn as sns
import matplotlib as plt
import data_analysis

sns.set(style="whitegrid", rc={"axes.edgecolor": "black", "axes.linewidth": 1})

# Single-client data analysis
sns.set_palette(data_analysis.PALETTE_SHORT)

data_analysis.single_client.latency_evaluation()
data_analysis.single_client.goodput_evaluation()
data_analysis.single_client.visibility_evaluation()


# Multi-client data analysis
sns.set_palette(data_analysis.PALETTE_FULL)

data_analysis.multi_client.latency_evaluation()
data_analysis.multi_client.read_throughout_evaluation()
data_analysis.multi_client.goodput_evaluation()
data_analysis.multi_client.visibility_evaluation()
data_analysis.multi_client.write_throughout_evaluation()
