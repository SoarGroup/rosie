import random, re, string
#"othello" "solitaireno" "blocksworld"

#gamelist=["8puzzle","3tower","tictactoeyes","9holesyes","4tower","5puzzle","mens3yes", "jmahjong", "picariayes", "zmaze", "frog", "othelloyes", "riskyes", "gbfox", "blocksworld"]
#scriptdir="river-crossing-scripts"
#scriptdir="2d-maze-scripts"
#scriptdir="tower-scripts"
#gamelist=["4toweri"]
scriptdir=""
#gamelist=["ubreakthrough"]
#gamelist=["2pushmaze"]
#gamelist=["econnect4"]
#gamelist=["solitaire"]
#gamelist=["cannibals"]
#gamelist=["tictactoe"]
gamelist=["husbands"]
#gamelist=["gbfoxnew"]
#gamelist=["cannibals"]
#gamelist=["iso8puzzle"]
#gamelist=["tictactoeyes"]
#gamelist=["9holes"]
#gamelist=["4towerc"]
#gamelist=["5puzzle"]
#gamelist=["mens3"]
#gamelist=["jmahjong"]
#gamelist=["picaria"]
#gamelist=["zmaze"]
#gamelist=["blocksworld"]
#gamelist=["frog"]
#gamelist=["othello"]
#gamelist=["risk"]
#gamelist=["gbfox"]
#gamelist=["15ipuzzle"]

#shuffled = random.sample(gamelist, 1)#len(gamelist))
shuffled = gamelist

newscript = ""
scriptname = ""
for game in shuffled:
    scriptname += game[0];
    if scriptdir != "":
        target = open(scriptdir + "/" + game + ".txt", 'r')
    else:
        target = open(game + ".txt", 'r')
    newscript+= target.read(10000)
    target.close()


result = open(scriptname + ".preparse", 'wb+')
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
