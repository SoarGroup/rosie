import glob, pprint, sys
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import os
import glob, pprint
import StringIO
import re

gFile = sys.argv[1]

dFile = open('solution.txt', 'r')

result = open(gFile + ".solution", 'wb+')

for line in dFile.readlines():
    line2 =  re.sub('[A-Z]\d+', "", line)
    result.write(line2)# + "\n");

result.close();
dFile.close();

