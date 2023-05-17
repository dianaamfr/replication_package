import seaborn as sns
import matplotlib as mpl
import visualization

mpl.rcParams['font.family'] = 'Verdana'
sns.set(font_scale=1.5)
sns.set_style("whitegrid", {'grid.linestyle': '--'})
sns.set_palette(visualization.COLORS)

visualization.latency_evaluation()
visualization.goodput_evaluation()
visualization.visibility_evaluation()
    