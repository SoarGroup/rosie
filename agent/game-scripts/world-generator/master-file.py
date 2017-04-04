import numpy as np
import os
import glob, pprint

result = open("game-state-change-respond.soar", 'wb+')

for infile in glob.glob('*.soar'):
    filename = infile.replace('.soar', '')
    if filename == "game-state-change-respond":
        continue;
    dFile = open(infile, 'r')
    stats = dFile.read(1000000);
    dFile.close();
    result.write(stats);
    result.write("\n");

result.close();
