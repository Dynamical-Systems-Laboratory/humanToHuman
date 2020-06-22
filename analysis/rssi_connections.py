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

x = np.array([i + .0000000001 for i in range(9)])
xfit = np.log10(np.arange(.01, 8, .01))
nexus_i8_mask = lambda df: (df['device_a'] == nexus) & (df['device_b'] == i8)
i8_nexus_mask = lambda df: (df['device_b'] == nexus) & (df['device_a'] == i8)
nexus_i10_mask = lambda df: (df['device_a'] == nexus) & (df['device_b'] == i10)
i10_nexus_mask = lambda df: (df['device_b'] == nexus) & (df['device_a'] == i10)
i8_i10_mask = lambda df: (df['device_a'] == i8) & (df['device_b'] == i10)
i10_i8_mask = lambda df: (df['device_b'] == i8) & (df['device_a'] == i10)

nexus_i8 = np.array([df['rssi'][nexus_i8_mask(df)].mean() for df in dfs])
i8_nexus = np.array([df['rssi'][i8_nexus_mask(df)].mean() for df in dfs])
nexus_i10 = np.array([df['rssi'][nexus_i10_mask(df)].mean() for df in dfs])
i10_nexus = np.array([df['rssi'][i10_nexus_mask(df)].mean() for df in dfs])
i10_i8 = np.array([df['rssi'][i10_i8_mask(df)].mean() for df in dfs])
i8_i10 = np.array([df['rssi'][i8_i10_mask(df)].mean() for df in dfs])

plt.ylabel('RSSI (dBm)')
plt.xlabel('AB Distance (meters)')

# Plots
plt.title('Measured RSSI vs Distance for Various Phone Pairs')
def curve(x, a, n):
    return 10 * x * n + a

# plt.plot(x, nexus_i8, label='android-8', mfc='r', marker='.')
# plt.plot(x, nexus_i10, label='android-10', mfc='b', mec='b', marker=',')
# fit,_ = curve_fit(curve, np.log10(x), nexus_i10, [20, 3])
# plt.plot(10 ** xfit, curve(xfit, *fit), mfc='b', mec='b', marker=',')

# plt.plot(x, i10_nexus, label='10-android', mfc='y', mec='y', marker=',')
# plt.plot(x, i8_nexus, label='8-android', mfc='g', marker='.', linestyle="None")

# plt.plot(x, i10_i8 , label='10-8', mfc='k', mec='k', marker=',')
# plt.plot(x, i8_i10, label='8-10', mfc='m', mec='m', marker=',')
# plt.legend()

# Diff and StDev
def diff_vec(As, Bs):
    return [a - b for (a, b) in zip(As, Bs)]

plt.title("Difference Between Android & iPhone 10 RSSI vs Distance")
diff = diff_vec(nexus_i10, i10_nexus)
plt.plot(x, diff, mfc='g', marker=',')
# print(statistics.stdev(diff))
plt.show()
