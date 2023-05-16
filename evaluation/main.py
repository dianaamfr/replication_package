import seaborn as sns
import matplotlib as mpl
from latency import latency_evaluation
from goodput import goodput_evaluation
# from visibility import visibility_evaluation

if __name__ == '__main__':
    mpl.rcParams['font.family'] = 'Verdana'
    sns.set(font_scale=1.5, style='whitegrid')
    sns.set_palette("icefire")

    latency_evaluation()
    goodput_evaluation()
    # visibility_evaluation()
    