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
result = open(gFile + ".tstats", 'r+')
oldtime = 1.0
newtime =1.0
for line in result.readlines():
    numbers_float = map(float, line.split())
    oldtime = numbers_float[1]
   
result.close();
    
wm = open("startend.seconds", 'r')

for line in wm.readlines():
    numbers_float = map(float, line.split())
    newtime = numbers_float[1]

wm.close();
if (oldtime > newtime):
    if ((oldtime-newtime)/oldtime*100.0) > 10.0:#only print if more then x%
        print ">>>>>>>>>  " + "{0:.2f}".format((oldtime-newtime)/oldtime*100.0) + " % Faster solution <<<<<<<<<<"
if (oldtime <= newtime):
    if ((newtime-oldtime)/oldtime*100.0) > 10.0:#only print if more then x%
        print ">>>>>>>>> " + "{0:.2f}".format((newtime-oldtime)/oldtime*100.0) + " % Slower solution <<<<<<<<<<<"
