import csv, re, string
import numpy as np
import sys
gFile = sys.argv[1] 

wm = open(gFile + ".pertaskstats", 'r')
stats = wm.read(100000);
wm.close();
wmsize = []

for line in stats.splitlines():
    if "WM size: " in line:
        wmsize.append(int(re.search('\d+', line).group()))
result = open(gFile + ".wmsize", 'wb+')

for elem in wmsize:
    result.write(str(elem) + "\n");

result.close();
    

