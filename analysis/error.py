import numpy as np
import pandas as pd
from scipy.optimize import curve_fit
import matplotlib.pyplot as plt
import statistics
import pickle

nexus = 5952679123360942499
i8 = 7132799880531636771
i10 = 7970601956863998406
dfs = []
for i in range(9):
    dfs.append(
        pd.read_csv(f'experiments/3-ground-3/{i}-meter.csv',
                    parse_dates=['time']))

meters = np.arange(0, 9, 1)
nexus_i8_mask = lambda df: (df['device_a'] == nexus) & (df['device_b'] == i8)
i8_nexus_mask = lambda df: (df['device_b'] == nexus) & (df['device_a'] == i8)
nexus_i10_mask = lambda df: (df['device_a'] == nexus) & (df['device_b'] == i10)
i10_nexus_mask = lambda df: (df['device_b'] == nexus) & (df['device_a'] == i10)
i8_i10_mask = lambda df: (df['device_a'] == i8) & (df['device_b'] == i10)
i10_i8_mask = lambda df: (df['device_b'] == i8) & (df['device_a'] == i10)

nexus_i8 = [df['rssi'][nexus_i8_mask(df)] for df in dfs]
i8_nexus = [df['rssi'][i8_nexus_mask(df)] for df in dfs]
nexus_i10 = [df['rssi'][nexus_i10_mask(df)] for df in dfs]
i10_nexus = [df['rssi'][i10_nexus_mask(df)] for df in dfs]
i10_i8 = [df['rssi'][i10_i8_mask(df)] for df in dfs]
i8_i10 = [df['rssi'][i8_i10_mask(df)] for df in dfs]

all_phones = [
    (nexus_i8, 'Android & iPhone 8'),
    (nexus_i10, 'Android & iPhone 10'),
    (i8_nexus, 'iPhone 8 & Android'),
    (i10_nexus, 'iPhone 10 & Android'),
    (i8_i10, 'iPhone 8 & iPhone 10'),
    (i10_i8, 'iPhone 10 & iPhone 8'),
]

def box_plots_normal_ignore_0_with_fit(meters, rssi2d, description, fit = True):
    ys = rssi2d[1:]
    Xmin = min(x.min() for x in rssi2d)
    Xmax = max(x.max() for x in rssi2d)

    plt.xlabel('RSSI (dBm)')
    plt.ylabel('AB Distance (meters)')
    plt.title('Distance vs Measured RSSI ' + description)

    plt.boxplot(rssi2d, positions = meters, vert = False)

    if not fit:
        plt.savefig(f"box_plots_{description}.png")
        plt.show()
        return

    n_exp = lambda x, a, n: 10 ** ((x - a) / n)
    (a, n), coef = curve_fit(n_exp, [y.mean() for y in ys], meters[1:],
                             p0 = [60,-30],xtol=0.001,maxfev=100000)
    n = n / 10

    curve_exp = lambda x: 10 * n * np.log10(x) + a
    xsmall = np.arange(Xmin, Xmax, .001)

    plt.plot(xsmall, n_exp(xsmall, a, 10 * n), mfc='b', mec='b', marker=',')
    plt.ylim([-1, 10])
    plt.figtext(-40, 8, f"a= {a}\nn= {n}\nr2= {coef[1,1]}")
    plt.savefig(f"box_plots_{description}.png")
    plt.show()

def shaded_area(meters, rssi2d, description):
    Xmin = min(x.min() for x in rssi2d)
    Xmax = max(x.max() for x in rssi2d)
    Xs = np.array([x.mean() for x in rssi2d])
    Xs_stdev = np.array([x.std() for x in rssi2d])
    n_exp = lambda x, a, n: 10 ** ((x - a) / n)

    (a, n10), coef = curve_fit(n_exp, Xs[1:], meters[1:],
                                xtol=0.001,maxfev=100000)
    n = n10 / 10

    plt.xlabel('RSSI (dBm)')
    plt.ylabel('AB Distance (meters)')
    plt.title('Distance vs Measured RSSI for ' + description)

    curve_exp = lambda x: n10 * np.log10(x) + a
    xsmall = np.arange(Xmin, Xmax, .001)
    shade_line = n_exp(Xs, a, n10)
    plt.fill_between(Xs, Xs_stdev + shade_line, -Xs_stdev + shade_line,
                     color = ('.6', '.6','.6'))
    plt.plot(xsmall, n_exp(xsmall, a, 10 * n), mfc='b', mec='b', marker=',')
    plt.savefig(f"shaded_area_{description}.png")
    plt.show()

def stdev_for_dist(meters, rssi2d):
    Ys_stdev = np.array([y.std() for y in rssi2d])
    plt.ylabel('RSSI St. Dev. (dBm)')
    plt.xlabel('AB Distance (meters)')
    plt.title('RSSI Standard Deviation vs Distance for Android & iPhone 10')
    plt.plot(meters, Ys_stdev, marker = ',')
    plt.show()


(data, name) = all_phones[1]
box_plots_normal_ignore_0_with_fit(meters, data, name)
