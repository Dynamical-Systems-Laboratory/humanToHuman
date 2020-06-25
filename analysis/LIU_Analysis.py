#!/usr/bin/env python
# coding: utf-8

# In[324]:


import pandas as pd
import matplotlib.pyplot as plt
import pickle
from scipy.optimize import curve_fit
import numpy as np


# In[404]:


i_8 = pickle.load(open('hello.pickle','rb'))

i_8 = np.array([np.array(x) for x in i_8])
x = [x.mean() for x in i_8[1:]]

#we use the mean, we'll compute the erron on y with another technique


# In[405]:


y = np.array([j for j in range(1,9)]) #real y
yl = np.array([np.log10(j) for j in range(1,9)]) #log10 of y


# In[406]:


plt.plot(x,y) #data


# In[407]:


plt.plot(x,yl) #data in linear scale


# In[408]:


def curve(x,a,b):
    return a*x-b
#linear curve
def expon(x,n,m):
    b = 1/n
    a = -b*m
    return 10**((x-a)/b)
#exponential inverse of lienar curve
def n_exp(x,a,b):
    return 10**((x-a)/b)
#simple exponential


# In[409]:


a,b = curve_fit(curve, x, yl, p0 = [-10,5]) #fitting linear problem


# In[410]:


print(a) #parameters
print(b) #covariances and stuff


# In[411]:


plt.plot(x, [10**curve(j,a[0],a[1]) for j in x],'x')
plt.plot(x,y,'.')
plt.show()
#exponent of linear problem

plt.plot(x, [expon(j,a[0],a[1]) for j in x],'x')
plt.plot(x,y,'.')
plt.show()
#exponent of found parameters


# In[412]:


c,d = curve_fit(n_exp, x, y,p0 = [60,-30],xtol=0.001,maxfev=100000)
#fitting exponential problem with tweaking


# In[413]:


print(c) #parameters
print(d) #var coeff


# In[414]:


plt.plot(x, [n_exp(j,c[0],c[1]) for j in x],'x')
plt.plot(x,y,'.')
plt.show()
#plotting real data against exponential curve


# In[ ]:





# In[ ]:




