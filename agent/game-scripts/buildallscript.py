import random, re, string

##needs fixing
# shuffle,survo,suko,sujiko,kakuro all cause of filled missing bug in goal
gamelist=[
"agbfox",
"blocksworld",
"colorken",
#"cannibals", #maybe?
#"dsokoban", #maybe?
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
#"usolitaireR",#maybe?
#"vsolitaire", #maybe?
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
#19 ^test3
#sometimesbroken
#17
#"ksideswap", #maybe? idk
#"othello"
#"familycross",
#needs fixing
]

length = len(gamelist)
print length

x = [[0 for i in range(length)] for j in range(length)]
print x
createdcount = 0
#MAKE LOOP here, 
while createdcount < (length*6 - 13):
    #print "trying"
    shuffled = random.sample(gamelist, length)
    ind = 0
    while ind < length:
        origid = gamelist.index(shuffled[ind])
        if x[origid][ind] > 6:
            break;
        ind+=1
    if ind != length:
        continue; #already have 3 results for one game slot make new one
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
        target = open(game + ".test5", 'r')
        newscript+= target.read(20000)
        newscript+= "\n";
        target.close()

    learnedlist = ""
    final = ""

    for line in newscript.splitlines():
        mylist = line.split(',')
        for sentence in mylist:
            if "name of an action" in sentence or "name of the goal" in sentence or "name of the action" in sentence or "name of a failure" in sentence:
                final += sentence + ".\n";
                if sentence in learnedlist: #don't need to learn
                    break;
                else:
                    learnedlist += sentence
            elif "then it is" in sentence or "then the location is" in sentence or "then the block is" in sentence:
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
