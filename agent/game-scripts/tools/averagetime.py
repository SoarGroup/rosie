import glob, pprint
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import os
import glob, pprint


#plt.yscale('log')
#plt.title('Efficiency by Teaching Order')

for infile in glob.glob('*.tstats'):
    filename = infile.replace('.tstats', '')
    dFile = open(infile, 'r')
    #stats = dFile.read(100000);
    
    arr=[]
    for line in dFile.readlines():
	numbers = map(float, line.split())
	arr.append(numbers)
        

    #print arr
    print filename
    print np.nanmean(arr, dtype=np.float64, axis=0)
    dFile.close();
  
#varData = np.var(matrix, axis=0)
