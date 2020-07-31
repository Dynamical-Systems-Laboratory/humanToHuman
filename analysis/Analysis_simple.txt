df_1 = pd.read_csv('tompkin_31_1.csv') #read

rssi_y = df_1[df_1['scanner']==7970197285653837963].rssi

step = '30s'
t_start = '09:42:00'
day = '2020-07-22 '

t1 = pd.Timestamp(day+t_start,tz='UTC')
t2 = t1+pd.Timedelta(step)
mean_y, std_y = [],[]
box_y = []
ft_x = [x for x in range(13)][::-1]
mt_x = [np.round(x*0.3048*2,2) for x in ft_x]

print(t1)

for i in range(13):
    data = rssi_y.between_time(t1.time(),t2.time()).tolist()
    print(data)
    t1=t2
    t2=t2+pd.Timedelta(step)
    mean_y.append(np.mean(data))
    std_y.append(np.std(data))
    box_y.append(data)
    
print(t2)

def n_exp(x,a,b):
    return 10**((x-a)/b)

par, cov = curve_fit(n_exp, mt_x, mean_y, sigma=std_y, p0=[60,-30], xtol=0.00001,maxfev=100000)

plt.boxplot(box_y, positions=mt_x)
plt.xticks(rotation=90)
plt.plot([n_exp(x, par[0], par[1]) for x in mt_x])
plt.show()