import random, re, string
import glob, pprint, sys
#import subprocess

#import numpy as np
#import matplotlib
#matplotlib.use('Agg')
#import matplotlib.pyplot as plt
import os
#import glob, pprint
import StringIO
#import re

game = sys.argv[1]

list1 = list(game)
list1[0] = list1[0].upper()
gamename = ''.join(list1)

target = open("dialog.txt", 'r')
newscript= target.read(40000)
newscript+= "\n";
target.close()

#result = open(scriptname + ".preparse", 'wb+')
learnedlist = ""
header = ""
final = ""

header += "<!doctype html>" + "\n";
header += "<html>" + "\n";
header += "<head>" + "\n";

header += "<title>" + gamename + "</title>" + "\n";
header += "</head>" + "\n";
header += "<body>" + "\n";
header += "<h1>" + gamename + "</h1>" + "\n";

header += "<br><b>Language: </b><a href=\"script.txt\">Teaching Script</a>&nbsp;&nbsp;&nbsp;<a href=\"dialog.txt\">Entire Dialog</a>" + "\n";
header += "<br><b>World states: </b>\n";

final += "<br><br><b><u>Primitive concepts used&nbsp;</u></b>" + "\n";
final += "<table>" + "\n";
final += "<tr><td>Object types:&nbsp;&nbsp;&nbsp;<br></td>" + "\n";
final += "<td>locations, blocks</td></tr>" + "\n";
final += "<tr><td>Colors:&nbsp;&nbsp;&nbsp;<br></td>" + "\n";
final += "<td>red, blue<br></td></tr>" + "\n";
final += "<tr><td>Spatial relations:&nbsp;&nbsp;&nbsp;</td>" + "\n";
final += "<td>on, below, between</td></tr>" + "\n";
final += "<tr><td>Functions:&nbsp;&nbsp;&nbsp;</td>" + "\n";
final += "<td>count of, less than</td></tr>" + "\n";
final += "</table>" + "\n";

final += "<br><h3>Teaching Dialog <b>(Rosie in bold)</b></h3>" + "\n";

for line in newscript.splitlines():
    if "TEACHER" in line:
        final += "<br>" + line + "\n";
    else :
        final += "<br><b>" + line + "</b>\n";
    if "load" in line or "Load" in line:
        lines = line.split();
        wfile = lines[2];
        
        #subprocess.run(["cp world-generator/" + wfile + "world learning-data/*/"+game+"/", ""])
        os.system("cp world-generator/" + wfile + "world learning-data/*/"+game+"/");
        header += "<a href=\"" + wfile + "world\">" +wfile +"world</a>&nbsp;" + "\n"
        
        exists = os.path.isfile(wfile + "png")
        if exists:
            final+= "<br><img src=\"" + wfile + "png\" alt=\"world representation\">" + "\n";
            #os.system("cp "+ wfile + "png learning-data/*/"+game+"/");
            
        
final += "<br><br><br>\n"
final += "</body>\n"
final += "</html>\n"

result2 = open("dialog.html", 'wb+')
result2.write(header)
result2.write(final)
result2.close();
