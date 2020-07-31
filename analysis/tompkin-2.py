import numpy as np
import pandas as pd
from scipy.optimize import curve_fit
from sklearn.metrics import r2_score
import matplotlib.pyplot as plt

df = pd.read_csv('experiments/tompkin-park-2/trials-1-4.csv') #read
df['time'] = pd.to_datetime(df['time'])
df = df.set_index('time')

# Filter by device
df = df[df['scanner'] == 5244994874468759163]
rssi = df['rssi'].sort_index()

rssi.plot()
plt.ylabel('RSSI (dBm)')
plt.xlabel('Time')
plt.title('Measured RSSI vs Time for iPhone 7 & Tablet')
plt.show()

# Filter by experiment
def filter_experiment(timestamp):
    start = pd.Timestamp(timestamp)
    step = pd.Timedelta('30s')
    ys = []

    current = start
    next_ = None # Not technically necessary

    for i in range(13):
        next_ = current + step
        for_distance = rssi.between_time(current.time(), next_.time())
        ys.append(for_distance)
        current = next_

    return ys

def unzip(l):
    l1, l2 = [], []
    for (a, b) in l:
        l1.append(a)
        l2.append(b)
    return l1, l2

def n_exp(x, a, n):
    return 10**((x - a) / 10 / n)

def fit_curve_a_n_r2(meters, rssi2d):
    x = [y.mean() for y in rssi2d]
    (a, n), coef = curve_fit(n_exp,
            x,
            meters,
            p0=[60, -30],
            xtol=0.001,
            maxfev=1000000)
    a_stdev = np.sqrt(coef[0, 0])
    n_stdev = np.sqrt(coef[1, 1])

    r2 = r2_score(n_exp(x, a, n), meters)

    return (a, a_stdev), (n, n_stdev), r2


def box_plots_normal_ignore_0_with_fit(meters, rssi2d, description, fit=True):
    ys = rssi2d[1:]
    Xmin = min(x.min() for x in rssi2d)
    Xmax = max(x.max() for x in rssi2d)

    plt.xlabel('RSSI (dBm)')#, fontsize=16)
    plt.ylabel('Distance (feet)')#, fontsize=16)
    plt.title('Distance vs Measured RSSI ' + description)

    plt.boxplot(rssi2d, positions=meters, vert=False)

    if not fit:
        plt.savefig(f"box_plots_{description}.png")
        plt.show()
        return

    (a, a_stdev), (n, n_stdev), r2 = fit_curve_a_n_r2(meters[1:], rssi2d[1:])
    xsmall = np.arange(Xmin, Xmax, .001)
    plt.plot(xsmall, n_exp(xsmall, a, n), mfc='b', mec='b', marker=',')
    plt.ylim([-1, 22])
    # plt.xticks(fontsize=14)
    # plt.yticks(fontsize=14)
    # plt.rcParams.update({'font.size': 16})
    text = f"""
    a= {round(a, 2)} ± {round(a_stdev, 2)}
    n= {round(n, 2)} ± {round(n_stdev, 2)}
    r2= {round(r2, 2)}
    """.strip().replace('    ', '')

    plt.text(-85, 16, text)
    print(description)
    plt.savefig(f"box_plots_{description}.png")
    plt.show()

def shaded_area(meters, rssi2d, description):
    Xmin = min(x.mean() for x in rssi2d)
    Xmax = max(x.mean() for x in rssi2d)
    Xs = np.array([x.mean() for x in rssi2d])
    Xs_stdev = np.array([x.std() for x in rssi2d])
    db_stdev = np.array([y.std() for y in rssi2d])

    (a, a_stdev), (n, n_stdev), r2 = fit_curve_a_n_r2(meters[1:], rssi2d[1:])
    deriv = np.log(10) / 10 / n * 10**((Xs_stdev - a) / 10 / n)
    m_stdev = np.sqrt(deriv * deriv * db_stdev * db_stdev)

    plt.xlabel('RSSI (dBm)')
    plt.ylabel('Distance (feet)')
    plt.title('Distance vs Measured RSSI for ' + description)

    text = f"""
    a= {round(a, 2)} ± {round(a_stdev, 2)}
    n= {round(n, 2)} ± {round(n_stdev, 2)}
    r2= {round(r2, 2)}
    """.strip().replace('    ', '')
    plt.text(-60, 8, text)

    xsmall = np.arange(Xmin, Xmax, .001)
    shade_line = n_exp(Xs, a, n)
    plt.fill_between(Xs,
                     shade_line + Xs_stdev,
                     shade_line - Xs_stdev,
                     color=('.6', '.6', '.6'))
    plt.plot(xsmall, n_exp(xsmall, a, n), color='b', marker=',')
    plt.plot(Xs, meters, mfc='k', mec='k', linestyle='None', marker='x')
    plt.savefig(f"shaded_area_{description}.png")
    plt.show()



exp_1_y = filter_experiment('2020-07-31 18:09:00')
exp_2_y = filter_experiment('2020-07-31 18:18:00')
exp_3_y = filter_experiment('2020-07-31 18:27:00')
exp_4_y = filter_experiment('2020-07-31 18:34:00')

exp = []
for idx in range(13):
    for_distance = pd.concat([ exp_1_y[idx], exp_2_y[idx], exp_3_y[idx], exp_4_y[idx] ])
    exp.append((2*idx,for_distance.sort_index()))

exp_x, exp_y = unzip(p for p in exp if len(p[1].index))

plt.plot(exp_x, [y.mean() for y in exp_y])
plt.ylabel('RSSI (dBm)')
plt.xlabel('AM Distance (feet)')
plt.title('Measured RSSI vs Distance for iPhone 7 & Tablet')
plt.show()


box_plots_normal_ignore_0_with_fit(exp_x, exp_y, "iPhone 7 & Tablet")

shaded_area(exp_x, exp_y, "iPhone 7 & Tablet")


