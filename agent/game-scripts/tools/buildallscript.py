import random, re, string

gamelix=[
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
#gamelist = ["queens","Mens3","xsudoku"]
#gamelist = ["nsudoku","Holes9","Cannibals"]
#gamelist = ["agbfox","1nrooks","Tictactoe"]

#gamelist = ["colorken","6nkings","husbands"]

gamelist = ["kenken","frog","8puzzle"]
# all related
#gamelist = ["agbfox","Cannibals","husbands"]
#gamelist = ["colorken","nsudoku","xsudoku"]
#gamelist = ["Tictactoe","Holes9","Mens3"]
#gamelist = ["queens","1nrooks","6nkings"]

length = len(gamelist)
print length

x = [[0 for i in range(length)] for j in range(length)]
print x
createdcount = 0
#MAKE LOOP here, 
#while createdcount < (length*7):
while createdcount < 50:
    #print "trying"
    shuffled = random.sample(gamelist, length)
    
    #ind = 0
    #bad = 0
    #while ind < length:
    #    origid = gamelist.index(shuffled[ind])
    #    if x[origid][ind] == 0:
    #        bad+=-60
    #    if x[origid][ind] > 2:
    #        bad+=12
    #    if x[origid][ind] > 3:
    #        bad+=25
    #    if x[origid][ind] > 4:
    #        bad+=35
    #    if x[origid][ind] > 5:
    #        bad+=60
    #    ind+=1
    #if bad > 0:
    #    continue;
    #if ind != length:
    #    continue; #already have 3 results for one game slot make new one
    ind = 0
    print createdcount
    while ind < length:
        origid = gamelist.index(shuffled[ind])
        x[origid][ind]+=1
        ind+=1
    # CREATED unique slot random parse
    newscript = ""
    scriptname = ""
    for game in shuffled:
        scriptname += game[0];
        target = open(game + ".testZ", 'r')
        newscript+= target.read(20000)
        newscript+= "\n";
        target.close()

    learnedlist = ""
    final = ""

    for line in newscript.splitlines():
        mylist = line.split(',')
        for sentence in mylist:
            if "name of an action" in sentence or "name of the goal" in sentence or "name of the action" in sentence or "name of a failure" in sentence or "name of the failure" in sentence or "name of a goal" in sentence:
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
                    learnedlist += sentence
            elif sentence:
                final += sentence + ".\n";
    #done building script
    result2 = open(scriptname + ".parse", 'wb+')
    result2.write(final)
    result2.close();
    createdcount+= 1

print x
#end
