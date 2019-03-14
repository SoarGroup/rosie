import random, re, string

##needs fixing
# shuffle,survo,suko,sujiko,kakuro all cause of filled missing bug in goal
#all in main comment out due to primitive adjacent failure? on movable/jumpable

gamelist=[
"I8puzzle",
"Blocksworld",
"Kstackedfrogs",
"Lazystackedfrogs",
"Tower5",
"Picaria",
"Holes9",
"Fifteenpuzzle",
"Mens3",
"Tictactoe",
"agbfox",
"blocksworld",
"colorken",
"cannibals",
"dsokoban", #maybe?
#"ektour", #maybe?
"frog",
"golf",
"husbands",
"ijigsawdoku",
"jmahjong",
"kenken",
"logi5",
"mapcolor",
"nsudoku",
#"othello",
"pyramid",
"queens",
"rtravelsales",
"stackedfrogs",
"tripeaks",
"usolitaireR",#maybe?
"vsolitaire", #maybe?
"wives",
"xsudoku",
"ysorting",
"zmaze",
#"0nknights", #maybe?
"1nrooks",
"2pushmaze",
"3tower",
"4tower",
"5puzzle",
"6nkings",
#"7kswap",
"8puzzle",
#sometimesbroken
#"ksideswap", #maybe? idk
#"othello",
#"familycross",
#needs fixing
]

shuffled = ["8men","frog"]
#"stackedfrogs"]


#gamelist=[
##"ubreakthrough",
#"kconnect3",
#"aconnect3",
#"econnect4",
##"15ipuzzleno",
##"x5towerno",
#"worldblocksno",
#"yiso5puzzleno",
#"iso8puzzleno",
##"picaria",
#"4towerno",
#"risk",
##"9holes",
##"mens3",
#"tictactoe",
#"blocksworldno"

print len(gamelist)
#shuffled = random.sample(gamelist, len(gamelist))
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
        elif "then it is" in sentence or "then the location is" in sentence or "then the block is" in sentence or "then they are" in sentence:
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
