import seaborn as sns
import matplotlib as plt
import visualization

sns.set(style="whitegrid", font_scale=1.2, rc={"axes.edgecolor": "black", "axes.linewidth": 1})
sns.set_palette(visualization.COLORS)

visualization.single_client.latency_evaluation()
visualization.single_client.goodput_evaluation()
visualization.single_client.visibility_evaluation()

# visualization.multi_client.latency_evaluation()
    