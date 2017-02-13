import csv, re, string
import numpy as np
import sys
#gFile = sys.argv[1] 


wm = open("endlearntime.txt", 'r')
stats = wm.read(100000);
wm.close();

data = []

data2 = []

for line in stats.splitlines():
    if "Total  CPU Time" in line:
        data.append(float(re.search('\d+\.\d+', line).group()))
    if " decisions" in line:
        data2.append(int(re.search('\d+ decisions', line).group().strip(" decisions")))
        
wm = open("stats.txt", 'r')
stats2 = wm.read(100000);
wm.close();

for line in stats2.splitlines():
    if "Total  CPU Time" in line:
        data.append(float(re.search('\d+\.\d+', line).group()))
    if " decisions" in line:
        data2.append(int(re.search('\d+ decisions', line).group().strip(" decisions")))

result = open("startend.seconds", 'a')
result2 = open("startend.decisions", 'a')

pastElem=0.0
for elem in data:            
    newresult= elem - pastElem
    result.write(str(newresult) + " ");
    pastElem = elem

pastElem=0
for elem2 in data2:
    newresult= elem2 - pastElem
    result2.write(str(newresult) + " ");
    pastElem = elem2

result.write("\n");
result.close();

result2.write("\n");
result2.close();
