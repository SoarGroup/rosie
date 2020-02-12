#!/usr/bin/env python
# a bar plot with errorbars
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from pylab import *

matplotlib.rcParams['ps.useafm'] = True
matplotlib.rcParams['pdf.use14corefonts'] = True
matplotlib.rcParams['text.usetex'] = True

N = 7
#no chunking at all in the first section, so not intra task chunkin benefit
data1 = (14,14,11,11,13,13,16)
data2 = (0.31525, 0.5195, 0.245, 0.444, 0.49475, 0.3, 0.36725)
data3 = (115.883,147.682,20.92,27.352,44.19,45.599,181.306)

ind = np.arange(N)  # the x locations for the groups
width = 0.2       # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind, data1, width,color="#3333ff")
rects2 = ax.bar(ind+width, data2, width,color="#006B3C")
rects3 = ax.bar(ind+width+width, data3, width, color='y')


# add some text for labels, title and axes ticks
ax.set_ylabel('Time (secs)/Words', fontsize=20)

ax.set_title('Tower of Hanoi version Comparison')
ax.set_xticks(ind+width)
#plt.yticks(range(0, 100, 10),  fontsize=16)
plt.ylim(0,185);
#plt.yscale('log')
ax.set_xticklabels(('1CFG','1CFS','1CLG','1CLS','2CLS','2CLG','2CFG') , rotation=-45, fontsize=10)

ax.legend( (rects1[0], rects2[0], rects3[0]), ('Words','Learning','Solving'), loc=2, fontsize=18, frameon=False)

plt.plot()
plt.savefig('towersolve.eps', format='eps', dpi= 2000)
plt.close()
