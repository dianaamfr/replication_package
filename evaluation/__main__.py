import seaborn as sns
import matplotlib as mpl
import visualization

mpl.rcParams['font.family'] = 'Verdana'
sns.set(font_scale=1.5)
sns.set_style("whitegrid", {'grid.linestyle': '--'})
sns.set_palette(visualization.COLORS)

visualization.single_client.latency_evaluation()
visualization.single_client.goodput_evaluation()
visualization.single_client.visibility_evaluation()

visualization.multi_client.latency_evaluation()
