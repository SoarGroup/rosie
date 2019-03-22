import random, re, string

##needs fixing
# shuffle,survo,suko,sujiko,kakuro all cause of filled missing bug in goal
#all in main comment out due to primitive adjacent failure? on movable/jumpable

gamelist=[
"agbfox",
"Atower5",
"blocksworld",
"Blocksworld",
"colorken",
"Cannibals",
"dsokoban", 
"Dshuffle",
"ektour", #b
"E8men",
"frog",
"Fifteenpuzzle",
"golf",
"husbands",
"Holes9",
"ijigsawdoku",
"I8puzzle",
"jmahjong", #b
"Jsujiko",
"kenken",
"Kstackedfrogs", #b
"logi5",
"Lazystackedfrogs", #b
"mapcolor",
"Mens3",
"nsudoku",
"othello",
"Osuko",
"pyramid", #b
"Picaria",
"queens", #b
"rtravelsales", #b
"Rsurvo",
"stackedfrogs", #b
"Swap",
"tripeaks",
"Tictactoe",
"usolitaireR", #b
"Ukakuro",
"vsolitaire", #b
"wives",
"xsudoku",
"Xbreakthrough",
"ysorting",
"zmaze",
"0nknights",
"1nrooks",
"2pushmaze",
"3tower",
"4tower",
"5puzzle",
"6nkings",
"7familycross",
"8puzzle",
"9ksideswap"
]

##"ubreakthrough",
#xshuffled = ["Osuko","Jsujiko","Rsurvo","Dshuffle","8puzzle","xsudoku","Ukakuro"]
shuffled = ["usolitaireR"]
#"Ubreakthrough"]
#"Lazystackedfrogs"]
#"8men","frog"]
#"stackedfrogs"]
#gamelist=[
#"kconnect3",
#"aconnect3",
#"econnect4",
#"risk",
#hexapawn

print len(gamelist)
#shuffled = random.sample(gamelist, len(gamelist))
#shuffled = random.sample(xshuffled, len(xshuffled))
#shuffled = gamelist
newscript = ""
scriptname = ""
for game in shuffled:
    scriptname += game[0];
    target = open(game + ".test", 'r')
    newscript+= target.read(20000)
    newscript+= "\n";
    target.close()


#result = open(scriptname + ".preparse", 'wb+')
learnedlist = ""
final = ""

for line in newscript.splitlines():
    mylist = line.split(',')
    for sentence in mylist:
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

#result2 = open(scriptname + ".parse", 'wb+')
result2 = open("soar-game.script", 'wb+')
result2.write(final)
result2.close();
