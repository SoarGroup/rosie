import random, re, string

gamelist=[
"agbfox",
"Atower5",
"blocksworld",
"Blocksworld",
"colorken",
"Cannibals",
#"dsokoban", #broke
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
"Kstackedfrogs", #broke
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
gamedir=[
"fox-goose-bean",
"tower-of-hanoi-5",
"blocks-world",
"blocks-world2",
"kenken",
"missionaries-and-cannibals",
"sokoban", 
"shuffle",
"knights-tour",
"8-men-on-a-raft",
"frogs-and-toads",
"fifteen-puzzle",
"golf-solitaire",
"jealous-husbands",
"nine-holes",
"jigsawdoku",
"eight-puzzle2",
"mahjong",
"sujiko",
"kenken2",
"king-stacking-frogs",
"logi-five",
"lazy-stacking-frogs",
"four-map-coloring",
"three-mens-morris",
"sudoku",
"mini-othello",
"suko",
"pyramid-solitaire",
"picaria",
"n-queens",
"traveling-salesman",
"survo",
"stacking-frogs",
"4-corner-knights",
"tri-peaks-solitaire",
"tic-tac-toe",
"peg-solitaire",
"kakuro",
"peg-solitaire2",
"jealous-wives",
"killer-sudoku",
"breakthrough",
"sorting",
"simple-maze",
"n-knights",
"n-rooks",
"pushing-maze",
"tower-of-hanoi",
"tower-of-hanoi-4",
"five-puzzle",
"n-kings",
"family-crossing",
"eight-puzzle",
"knight-swapping"
]

#shuffled = ["Tictactoe"]

print len(gamelist)
#shuffled = random.sample(gamelist, len(gamelist))
shuffled = gamelist

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
