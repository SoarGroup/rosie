import csv, re, string
import numpy as np
import sys
gFile = sys.argv[1] 

wm = open(gFile + ".pertaskstats", 'r')
stats = wm.read(900000);
wm.close();
data = []

for line in stats.splitlines():
    if "Total  CPU Time" in line:
        data.append(float(re.search('\d+\.\d+', line).group()))

result = open(gFile + ".seconds", 'wb+')

pastElem=0.0
for elem in data:
    newresult= elem - pastElem
    result.write(str(newresult) + "\n");
    pastElem = elem
result.close();
    

