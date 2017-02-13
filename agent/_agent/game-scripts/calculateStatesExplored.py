import csv, re, string
import numpy as np
import sys

wm = open("statesexp.txt", 'r')
stats = wm.read(100000);
wm.close();

data = []

for line in stats.splitlines():
    if ":" in line:
        data.append(int(re.search('\d+:', line).group().strip(":")))
        
result = open("states.txt", 'a')

for elem in data:
    result.write(str(elem) + "\n");

result.close();
