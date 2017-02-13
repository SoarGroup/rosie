import csv, re, string
import numpy as np
import sys

gFile = "bwtemplate"

#gFile = sys.argv[1]




wm = open(gFile + ".world", 'r')
stats = wm.read(100000);
wm.close();

objid = []
predid = []

result = open(gFile + ".soar", 'wb+')

result.write("sp {dialog-event*apply*game-state-change-respond*" + gFile + "\n");
result.write("   (state <s> ^name dialog-event\n");
result.write("              ^top-state <ts>\n");
result.write("              ^operator <o>)\n");
result.write("   (<o> ^name game-state-change-respond\n")
result.write("        ^type " + gFile + ")\n");
result.write("   (<ts> ^world <wo2>)\n")
result.write("-->\n")
result.write("   (<ts> ^world <wo2> -)\n")
result.write("   (<ts> ^world <wo>)\n")
result.write("   (<wo> ^objects <objs> ^predicates <preds> ^robot <ro>)\n")
result.write("   (<ro> ^handle rosie ^item-type object ^arm.action wait ^predicate.handle rosie)\n")

result.write("   (<objs> ^object <self>")
#add objects
for line in stats.splitlines():
    if "obj" in line:
        chunks = line.split()
        #chunks = re.search('obj \d+', line).group().strip("obj ")
        objid.append("<o"+str(chunks[1]) + ">")
        result.write(" <o" + str(chunks[1])+ ">")
result.write(")\n")

result.write("   (<self> ^type object ^handle self ^predicates.type object)\n")
result.write("   (<preds> ^predicate ")
#add predicates
for line in stats.splitlines():
    if "predicate " in line:
        chunks = line.split()
        predid.append(str(chunks[1]))
        result.write(" <" + str(chunks[1])+ ">")
#add predicate sets
for line in stats.splitlines():
    if "predicate-set " in line:
        chunks = line.split()
        predid.append(str(chunks[1]))
        result.write(" <" + str(chunks[1])+ ">")

flag = 0
index = -1
index2 = -1

predicates = ""
for line in stats.splitlines():
    if "obj" in line:
        result.write(")\n")
        index= index +1
        result.write("(" + str(objid[index])+ " ^item-type object ^handle object-" + str(index) + " ^predicates <pr" + str(index) + ">)\n")
        result.write("(<pr" + str(index) + "> ^visible true ")
        flag = 1        
    elif "predicate " in line:
        if flag == 1:
            index=-1
        result.write(")\n" + predicates)
        predicates = ""
        index= index +1
        result.write("(<" + str(predid[index])+ "> ^item-type predicate ^handle "
                     + str(predid[index]) + " ^instance ")
        flag = 2
    elif "predicate-set " in line:
        if flag == 1:
            index=-1
        result.write(")\n" + predicates)
        predicates = ""
        index= index +1
        result.write("(<" + str(predid[index])+ "> ^item-type predicate ^handle "
                     + str(predid[index]) + " ^instance ")
        flag = 3
    elif line.strip():
        if flag == 1:
            words = line.split()
            result.write("^" + str(words[0]) + " " + str(words[1]) + " ")
        elif flag == 2:
            index2 = index2 +1
            words = line.split()
            result.write("<in" + str(index2) + "> ")
            predicates+= "(<in" + str(index2) + "> ^1 <o" + str(words[0]) + "> ^2 <o" + str(words[1]) + ">)\n"
        elif flag == 3:
            index2 = index2 +1
            
            result.write("<in" + str(index2) + "> ")
            
            predicates+= "(<in" + str(index2) + "> ^1 <set" + str(index2) + ">)\n(<set" + str(index2) + ">  ^object"
            
            for word in line.split():
               predicates+= " <o" +  str(word) + ">"
            predicates +=")\n"

result.write(")\n" + predicates)            
result.write("}\n")

result.close();
