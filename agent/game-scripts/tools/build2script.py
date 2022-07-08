import random, re, string

gamelist=[
#"ubreakthrough",
"othello",
"lfamilycrossno",
#"2pushmazeno",
#"dsokobanno",
"kconnect3",
"aconnect3",
"econnect4",
#"15ipuzzleno",
#"solitaireno",
#"x5towerno",
"worldblocksno",
"yiso5puzzleno",
"iso8puzzleno",
"8puzzleno",
"5puzzleno",
"cannibalsno",
"husbandsno",
#"frogno",
#"picaria",
"3towerno",
"4towerno",
"jmahjongno",
"gbfoxno",
"risk",
#"9holes",
#"mens3",
"tictactoe",
"blocksworldno",
"zmazeno"]

#o8p4chfj5gr9mtbz3
print len(gamelist)
#shuffled = random.sample(gamelist, len(gamelist))
shuffled = gamelist

newscript = ""
scriptname = ""
for game in shuffled:
    scriptname += game[0];
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
        elif "then it is" in sentence or "then the location is" in sentence or "then the block is" in sentence:
            if sentence in learnedlist:
                continue
            else:
                result.write(sentence + ".\n")

                if "matched" in sentence and "matched" not in learnedlist:
                    result.write("ok.\n")
                learnedlist += sentence
        else:
            result.write(sentence + ".\n")
            if "matched" in sentence and "matched" in learnedlist:
                result.write("ok.\n")

result.close();
