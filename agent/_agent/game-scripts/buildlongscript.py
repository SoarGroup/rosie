import random, re, string
#"othello" "solitaireno" "4towerno"
gamelist=["8puzzleno","3towerno","tictactoe","5puzzleno", "jmahjongno", "zmazeno", "blocksworldno", "kconnect3", "aconnect3"]

i = 0
newscript = ""
#scriptname = ""
while (i<1):
    i+=1
    shuffled = random.sample(gamelist, len(gamelist))

    for game in shuffled:
        #scriptname += game[0];
        target = open(game + ".txt", 'r')
        newscript+= target.read(1000)
        target.close()


result = open("long.preparse", 'wb+')
learnedlist = ""

index = 0
indexb = 0
for line in newscript.splitlines():
    mylist = line.split(',')
    for sentence in mylist:
        if "name of an action" in sentence or "name of the goal" in sentence or "name of the action" in sentence or "name of a failure" in sentence:
            #if (random.randint(0,1) > 0): #random 50%different action name
            sentence+= str(indexb) + str(indexb)
            result.write(sentence +".\n")
            if sentence in learnedlist: #don't need to learn #could do bad match?
                break;
            else:
                learnedlist += sentence
        elif "then it is" in sentence or "then the location is" in sentence or "then the block is" in sentence:
            #index = random.randint(0,5)
            #test without new concepts first
            if sentence in learnedlist:
                continue
            else:
                result.write(sentence + ".\n")
                learnedlist += sentence
        else:
           # if "game is othello" in sentence:
            #    indexb+=2
            if "name of the puzzle" in sentence or "name of the game" in sentence:
                result.write(sentence + str(index) + str(index) + ".\n")
                if ((index/2 % 15) == 0):
                    indexb+=2
                index+=2
                
            else:
                result.write(sentence + ".\n")

result.close();
