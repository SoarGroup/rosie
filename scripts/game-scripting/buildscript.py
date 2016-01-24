import random, re, string

#gamelist = ["3mens", "tictactoe"];
gamelist=["3mens", "tictactoe", "8puzzle", "5puzzle", "4tower", "3tower", "picaria", "9holes"]
shuffled = random.sample(gamelist, len(gamelist))
newscript = str(shuffled)
newscript += "\n"
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
    for sentence in mylist:
        if "name of an action" in sentence or "name of the goal" in sentence or "name of the action" in sentence or "name of a failure" in sentence:
            result.write(sentence + "\n")
            if sentence in learnedlist: #don't need to learn
                break;
            else:
                learnedlist += sentence
        elif "then it is" in sentence:
            if sentence in learnedlist:
                #result.write("yes\n")# ASSUME unique names for test noyesalways
                continue;
            else:
                result.write(sentence + "\n")
                learnedlist += sentence
        else:
            result.write(sentence + "\n")

result.write("END")
result.close();
