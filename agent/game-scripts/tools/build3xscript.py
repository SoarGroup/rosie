import random, re, string

scriptdir="tower-scripts"
gamelist=["3tower1CLS","3tower1CLG","3tower1CFS","3tower1CFG","3tower2CFG","3tower2CLS","3tower2CLG"]
#"4tower2BFS","4tower2BFG"]

shuffled = gamelist

for game in shuffled:
    scriptname = game
    if scriptdir != "":
        target = open(scriptdir + "/" + game + ".txt", 'r')
    else:
        target = open(game + ".txt", 'r')
    newscript= target.read(10000)
    target.close()
    result = open(scriptname + ".3tower", 'wb+')
    learnedlist = ""

    for line in newscript.splitlines():
        mylist = line.split(',')
        for sentence in mylist:
            if "name of an action" in sentence or "name of the goal" in sentence or "name of the action" in sentence or "name of a failure" in sentence:
                result.write(sentence + ".\n")
                if sentence in learnedlist: #don't need to learn
                    break;
                else:
                    learnedlist += sentence
            elif "then it is" in sentence or "then the location is" in sentence:
                if sentence in learnedlist:
                    continue
                else:
                    result.write(sentence + ".\n")
                    learnedlist += sentence
            else:
                result.write(sentence + ".\n")
                        
    result.close();
