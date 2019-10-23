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
"ektour",
"E8men",
"frog",
"Fifteenpuzzle",
"golf",
"Gkingtour",
"husbands",
"Holes9",
"ijigsawdoku",
"I8puzzle",
"jmahjong",
"Jsujiko",
"kenken",
"Kstackedfrogs", 
"logi5",
"Lazystackedfrogs", 
"mapcolor",
"Mens3",
"nsudoku",
"Nbishops",
"othello",
"Osuko",
"pyramid", 
"Picaria",
"queens",
"rtravelsales",
"Rsurvo",
"stackedfrogs",
"Swap",
"tripeaks",
"Tictactoe",
"usolitaireR",
"Ukakuro",
"vsolitaire",
"Vconnect4",
"wives",
"Wconnect3",
"xsudoku",
"Xbreakthrough",
"ysorting",
"zmaze",
"Zmanager",
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
#xshuffled = ["Osuko","Jsujiko","Rsurvo","Dshuffle","8puzzle","xsudoku","Ukakuro"]
#shuffled = ["colorken","nsudoku","xsudoku"]
#shuffled = ["Tictactoe","Holes9","Mens3"]
#shuffled = ["queens","1nrooks","6nkings"]
#shuffled = ["pyramid"]
#shuffled = ["ektour","othello", "Kstackedfrogs"]
#shuffled = ["2pushmaze","Kstackedfrogs"]
#shuffled = ["Kstackedfrogs"]
#shuffled = ["Lazystackedfrogs"]
#shuffled = ["Cannibals","husbands","agbfox"]
#shuffled = ["Gkingtour", "Nbishops"]
#shuffled = ["Nbishops"]
#shuffled = ["Zmanager"]
#shuffled = ["pyramid","Holes9"]
#shuffled = ["Holes9"]
#shuffled = ["Mens3","queens","xsudoku"]
#shuffled = ["Wconnect3"]
#new
#"nbishops"]
#"Gkingtour"]
###########################
########################
#gamelist=[
#"kconnect3",
#"aconnect3",
#"econnect4",
#"risk",
#"hexapawn"

print len(gamelist)
shuffled = random.sample(gamelist, len(gamelist))
#shuffled = random.sample(xshuffled, len(xshuffled))
#shuffled = gamelist
newscript = ""
scriptname = ""
for game in shuffled:
    scriptname += game[0];
    target = open(game + ".testd", 'r')
    newscript+= target.read(20000)
    newscript+= "\n";
    target.close()
    

#result = open(scriptname + ".preparse", 'wb+')
learnedlist = ""
final = ""

for line in newscript.splitlines():
    mylist = line.split(',')
    for sent in mylist:
        list1 = list(sent)
        list1[0] = list1[0].upper()
        sentence = ''.join(list1)
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
