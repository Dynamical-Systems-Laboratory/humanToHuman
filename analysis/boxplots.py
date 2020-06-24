import numpy as np
import pandas as pd
from scipy.optimize import curve_fit
import matplotlib.pyplot as plt
import statistics

nexus = 5952679123360942499
i8 = 7132799880531636771
i10 = 7970601956863998406
dfs = []
for i in range(9):
    dfs.append(
        pd.read_csv(f'experiments/3-ground-3/{i}-meter.csv',
                    parse_dates=['time']))

x = np.arange(0, 9)
nexus_i8_mask = lambda df: (df['device_a'] == nexus) & (df['device_b'] == i8)
i8_nexus_mask = lambda df: (df['device_b'] == nexus) & (df['device_a'] == i8)
nexus_i10_mask = lambda df: (df['device_a'] == nexus) & (df['device_b'] == i10)
i10_nexus_mask = lambda df: (df['device_b'] == nexus) & (df['device_a'] == i10)
i8_i10_mask = lambda df: (df['device_a'] == i8) & (df['device_b'] == i10)
i10_i8_mask = lambda df: (df['device_b'] == i8) & (df['device_a'] == i10)

nexus_i8 = [df['rssi'][nexus_i8_mask(df)] for df in dfs]
i8_nexus = [df['rssi'][i8_nexus_mask(df)] for df in dfs]
nexus_i10 = np.array([np.array(df['rssi'][nexus_i10_mask(df)]) for df in dfs])
i10_nexus = [df['rssi'][i10_nexus_mask(df)] for df in dfs]
i10_i8 = [df['rssi'][i10_i8_mask(df)] for df in dfs]
i8_i10 = [df['rssi'][i8_i10_mask(df)] for df in dfs]

def box_plots_normal():
    plt.ylabel('RSSI (dBm)')
    plt.xlabel('AB Distance (meters)')
    plt.title('Measured RSSI vs Distance for Various Phone Pairs')
    plt.boxplot(nexus_i10, positions = x)
    plt.show()

def box_plots_log():
    plt.ylabel('RSSI (dBm)')
    plt.xlabel('Log AB Distance (log meters)')
    plt.title('Measured RSSI vs Log Distance for Various Phone Pairs')
    plt.boxplot(nexus_i10, positions = np.log10(x + .000001))
    plt.show()

def box_plots_log_ignore_0():
    plt.ylabel('RSSI (dBm)')
    plt.xlabel('Log AB Distance (log meters)')
    plt.title('Measured RSSI vs Log Distance for Various Phone Pairs')
    plt.boxplot(nexus_i10[1:], positions = np.log10(x[1:] + .000001))
    plt.show()

def box_plots_log_ignore_0_with_fit():
    plt.ylabel('RSSI (dBm)')
    plt.xlabel('Log AB Distance (log meters)')
    plt.title('Measured RSSI vs Log Distance for Various Phone Pairs')
    xlog = np.log10(x[1:] + .000001)
    ys = nexus_i10[1:]
    plt.boxplot(ys, positions = xlog)
    def curve(x, m, b):
        return m * x + b

    (m, b), _ = curve_fit(curve, xlog, [y.mean() for y in ys], [10, 3])
    plt.plot(xlog, curve(xlog, m, b), mfc='b', mec='b', marker=',')
    a = b / m
    n = 1 / 10 / m

    print(a, n)

    plt.show()

def box_plots_normal_ignore_0_with_fit():
    xlog = np.log10(x[1:])
    ys = nexus_i10[1:]
    def curve(x, m, b):
        return m * x + b

    (m, b), _ = curve_fit(curve, xlog, [y.mean() for y in ys])
    a = b / m
    n = 1 / 10 / m

    print(a, n)

    plt.ylabel('RSSI (dBm)')
    plt.xlabel('AB Distance (meters)')
    plt.title('Measured RSSI vs Distance for Various Phone Pairs')
    plt.boxplot(nexus_i10, positions = x)

    def curve_exp(x):
        return 10 ** ((x - a) / 10 / n)

    xsmall = np.arange(.001, 9, .001)
    plt.plot(xsmall, curve_exp(xsmall), mfc='b', mec='b', marker=',')


    plt.show()



box_plots_normal_ignore_0_with_fit()

