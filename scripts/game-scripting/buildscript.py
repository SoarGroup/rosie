import random, re, string

gamelist = ["3tower","8puzzle","5puzzle","mens3"]
#gamelist=["3tower","tictactoe","9holes","4tower","5puzzle","8puzzle","mens3", "picaria"]
#gamelist=["mens3", "tictactoe", "8puzzle", "5puzzle", "4tower", "3tower", "picaria", "9holes"]
shuffled = gamelist #random.sample(gamelist, len(gamelist))

newscript = ""
scriptname = ""
for game in shuffled:
    scriptname += game[0];
    target = open(game + ".txt", 'r')
    newscript+= target.read(1000)
    target.close()


result = open(scriptname + ".script", 'wb+')
learnedlist = ""
for line in newscript.splitlines():
    mylist = line.split(',')
    extras = []
    for sentence in mylist:
        if "name of an action" in sentence or "name of the goal" in sentence or "name of the action" in sentence or "name of a failure" in sentence:
            result.write(sentence + ".\n")
            if sentence in learnedlist: #don't need to learn
                break;
            else:
                learnedlist += sentence
        elif "then it is" in sentence or "then the location is" in sentence:
            if sentence in learnedlist:
                extras.append(sentence)
            elif "is matched" in sentence and "is matched" in learnedlist:
                extras.append(sentence)
            else:
                result.write(sentence + ".\n")
                learnedlist += sentence
        else:
            result.write(sentence + ".\n")

    for extra in extras:
        if extra in learnedlist:
                result.write("yes.\n")# ASSUME unique names for test noyesalways
                continue
        elif "is matched" in sentence and "is matched" in learnedlist:
                #for nontransfer concepts, only have matched now
                result.write("no.\n")
                result.write(sentence + ".\n")
                learnedlist += sentence

result.close();
