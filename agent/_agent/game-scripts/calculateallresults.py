import glob, os
#analyze data
path = 'game-scripts'
for filename in glob.glob(os.path.join(path, '*.script')):
    read = open(filename, 'r')

gamelist=["8puzzle","iso8puzzle","othello","3tower","tictactoe","9holes","4tower","5puzzle","mens3", "picaria"]
shuffled = random.sample(gamelist, len(gamelist))

newscript = ""
scriptname = ""
for game in shuffled:
    scriptname += game[0];
    target = open(game + ".txt", 'r')
    newscript+= target.read(1000)
    target.close()


result = open(scriptname + ".script", 'wb+')
learnedlist = ""
old = " "
forget = " "
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
            elif "is matched" in sentence:
                old += sentence
                result.write(sentence + ".\n")
                learnedlist += sentence
            else:
                result.write(sentence + ".\n")
                learnedlist += sentence
        else:
            result.write(sentence + ".\n")

    for extra in extras:
        if extra in learnedlist and extra not in forget:
                result.write("yes.\n")
                continue
        elif "is matched" in extra and "is matched" in learnedlist:
                #for nontransfer concepts, only have matched now
                result.write("no.\n")
                result.write(extra + ".\n")
                forget+= old
                learnedlist += extra
result.close();
