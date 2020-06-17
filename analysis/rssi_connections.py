import pandas as pd
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

x = [i for i in range(9)]
nexus_i8_mask = lambda df: (df['device_a'] == nexus) & (df['device_b'] == i8)
i8_nexus_mask = lambda df: (df['device_b'] == nexus) & (df['device_a'] == i8)
nexus_i10_mask = lambda df: (df['device_a'] == nexus) & (df['device_b'] == i10)
i10_nexus_mask = lambda df: (df['device_b'] == nexus) & (df['device_a'] == i10)
i8_i10_mask = lambda df: (df['device_a'] == i8) & (df['device_b'] == i10)
i10_i8_mask = lambda df: (df['device_b'] == i8) & (df['device_a'] == i10)

nexus_i8 = [df['rssi'][nexus_i8_mask(df)].mean() for df in dfs]
i8_nexus = [df['rssi'][i8_nexus_mask(df)].mean() for df in dfs]
nexus_i10 = [df['rssi'][nexus_i10_mask(df)].mean() for df in dfs]
i10_nexus = [df['rssi'][i10_nexus_mask(df)].mean() for df in dfs]
i10_i8 = [df['rssi'][i10_i8_mask(df)].mean() for df in dfs]
i8_i10 = [df['rssi'][i8_i10_mask(df)].mean() for df in dfs]

plt.ylabel('dBm')
plt.xlabel('AB meters')

# Plots
# plt.title('Measured RSSI vs Distance for Various Phone Pairs')
# 
# plt.plot(x, nexus_i8, label='nexus-8', mfc='r', marker=',')
# plt.plot(x, nexus_i10, label='nexus-10', mfc='b', marker=',')
# 
# plt.plot(x, i10_nexus , label='10-nexus', mfc='y', marker=',')
# plt.plot(x, i8_nexus, label='8-nexus', mfc='g', marker=',')
# 
# plt.plot(x, i10_i8 , label='10-8', mfc='k', marker=',')
# plt.plot(x, i8_i10, label='8-10', mfc='m', marker=',')
# plt.legend()

# Diff and StDev
def diff_vec(As, Bs):
    return [a - b for (a, b) in zip(As, Bs)]

# plt.title("Difference Between iPhone 8 RSSI and iPhone 10 RSSI vs Distance")
# diff = diff_vec(nexus_i10, i10_nexus)
# plt.plot(x, diff, mfc='g', marker=',')
# print(statistics.stdev(diff))

plt.show()
