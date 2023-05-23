import pandas as pd
import matplotlib.pyplot as plt
from visualization.utils import PATH, CC_CLIENTS_DIR, LOCAL_REGION, CLIENTS
from visualization.utils import get_data, df_describe

CC_LATENCY_PATH = PATH + '/logs/latency' + CC_CLIENTS_DIR + '/c_'

RESULT_PATH = PATH + '/results/latency'


def latency_evaluation():
    dfs = []
    for i, clients_number in enumerate(CLIENTS):
        df = get_latency_times(clients_number)
        df['clients'] = clients_number
        dfs.append(df)

    df = pd.concat(dfs).reset_index(drop=True)
    latency_distribution_table(df)


def get_latency_times(clients_number):
    df = get_data(CC_LATENCY_PATH + str(clients_number), 'readclient-' + LOCAL_REGION)

    return df.groupby('id').apply(
        lambda group:
            group.loc[group['logType'] == 'ROT_RESPONSE', 'time'].values[0] -
            group.loc[group['logType'] == 'ROT_REQUEST', 'time'].values[0]
    ).reset_index(name='latency').sort_values('id').reset_index(drop=True)


def latency_distribution_table(df):
    df_result = df.groupby('clients').apply(df_describe, 'latency').round(2).reset_index(drop=True)

    plt.table(cellText=df_result.values, colLabels=df_result.columns,
              cellLoc='center', loc='center')
    plt.axis('off')
    plt.annotate('Latency (ms)', (0.5, 0.7), xycoords='axes fraction',
                 ha='center', va='bottom', fontsize=10)
    plt.savefig(RESULT_PATH + '/clients_latency_table.png', dpi=300, bbox_inches='tight')
    plt.clf()
    plt.close()

