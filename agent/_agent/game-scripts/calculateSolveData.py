import csv, re, string
import numpy as np

#gamelist=["mens3", "tictactoe", "8puzzle", "5puzzle", "4tower", "3tower", "picaria", "9holes"]

#automate here
#gFile = "mp83549t"
#gFile = "t94538pm"
#gFile = "tp538m94"
gFile = "9i8pt5m43"
with open(gFile + ".csv", 'rb') as f:
    reader = csv.reader(f)
    next(reader)
    stats = list(reader)


rosie = open(gFile + ".rosie", 'r')
gametimes = rosie.read(10000);
rosie.close();
gtimes = []
for line in gametimes.splitlines():
    if "solve the puzzle" in line or "easy" in line:
        gtimes.append(int(re.search(r'\d+', line).group()))

print gtimes

results = ""

i = 1
j = 0
count = 0.0
sumDc = 0.0
result = open(gFile + ".solveresult", 'wb+')
for line in stats:
    if (j > 9):
        break;
    if i < int(gtimes[j]):
        i+=1
        continue;
    sumDc+= int(line[1])
    count+= 1
    
    if (i == int(gtimes[j+1])):
        result.write(str(sumDc/count/1000.0) + "\n");
        sumDc = 0.0
        count = 0.0
        j += 2
    i+= 1

f.close()
result.close()
