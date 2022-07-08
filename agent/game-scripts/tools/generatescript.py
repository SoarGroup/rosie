import random, re, string
import glob, pprint, sys

#import numpy as np
#import matplotlib
#matplotlib.use('Agg')
#import matplotlib.pyplot as plt
import os
#import glob, pprint
import StringIO
#import re


gFile = sys.argv[1]

target = open(gFile + ".test", 'r')
newscript= target.read(20000)
newscript+= "\n";
target.close()

#result = open(scriptname + ".preparse", 'wb+')
learnedlist = ""
final = ""

for line in newscript.splitlines():
    mylist = line.split(',')
    for sent in mylist:
        list1 = list(sent)
        list1[0] = list1[0].upper()
        sentence = ''.join(list1)
        if "name of an action" in sentence or "name of the goal" in sentence or "name of the action" in sentence or "name of a failure" in sentence or "name of the failure" in sentence or "name of a goal" in sentence:
            #result.write(sentence + ".\n")
            final += sentence + ".\n";
            if sentence in learnedlist: #don't need to learn
                break;
            else:
                learnedlist += sentence
        elif "then it is" in sentence or "then the location is" in sentence or "then the block is" in sentence or "then they are" in sentence or "then the object is" in sentence:
            if sentence in learnedlist:
                continue;
            else:
                final += sentence + ".\n";
                #result.write(sentence + ".k.\n")
                learnedlist += sentence
        elif sentence:
            final += sentence + ".\n";
            #result.write(sentence + ".d.\n")
        
#result.close();
result2 = open("soar-game.script", 'wb+')
result2.write(final)
result2.close();
