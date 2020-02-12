import numpy as np
import matplotlib.pyplot as plt
import os
import glob, pprint

matrixT = []

i = 0;
for infile in glob.glob('*.seconds'):
    dFile = open(infile, 'r')
    stats = dFile.read(100000);
    dFile.close();
    matrixT.append([])
    for line in stats.splitlines():
        if line.strip():
            matrixT[i].append(float(line))
    i+=1

meanData1 = np.mean(matrixT, axis=0)
meanDataT = meanData1*1000.0
#print meanDataT 
matrixD = []

i = 0;
for infile in glob.glob('*.avgdc'):
    dFile = open(infile, 'r')
    stats = dFile.read(100000);
    dFile.close();
    matrixD.append([])
    for line in stats.splitlines():
        if line.strip():
            matrixD[i].append(float(line))
    i+=1

meanDataD = np.mean(matrixD, axis=0)
#varData = np.var(matrix, axis=0)

#print meanDataD
#exit()
t = (0,1,2,3,4,5,6,7,8,9)
#print t
#pC = (16, 17, 18, 19)
#pI = (50, 53, 57, 58)
#pT = (950,820,600,550)

plt.yscale('log')
plt.title('Agent Efficiency by Task Order')
plt.ylabel('Avg. Time(msecs)', fontsize=18)
plt.xlabel('Learning order position', fontsize=18)

# red dashes, blue squares and green triangles
perC, = plt.plot(t, meanDataD, 'rs')
perT, =plt.plot(t, meanDataT, 'g^')

plt.legend((perC,perT), ('per cycle','per task'), bbox_to_anchor=(1.05, 1), loc=2, borderaxespad=0., fontsize=18 )
plt.savefig('taskAvgTime.eps', format='eps', dpi= 300)
plt.close()
