import seaborn as sns
import matplotlib as mpl
import visualization

mpl.rcParams['font.family'] = 'Verdana'
sns.set(font_scale=1.5, style='whitegrid')
palette = ["#006f73", "#ff7349"]
sns.set_palette(palette)

# visualization.latency_evaluation()
# visualization.goodput_evaluation()
visualization.visibility_evaluation()
    