#!/bin/bash

suffix=".tower"
tstats=".tstats"
states=".statesexp"
top=".top"
dec=".decisions"
sol=".solution"
stored="stored-"
statesexp=".statesexp"
repeat=1
ffix="no"
chunks=".chunks"

#puzzles
#declare -a arr=("husbands" "cannibals" "lfamilycross" "15ipuzzle" "x5tower" "zmaze" "jmahjong" "gbfox" "3tower" "4tower" "5puzzle" "8puzzle" "iso8puzzle" "yiso5puzzle" "blocksworld" "worldblocks")
declare -a arr=("husbands")
#broken: frog, solitaire, 2pushmaze, dsokobanno, 

## now loop through the above array
c=1
for game in "${arr[@]}"
do
	echo "Testing game: " $game
	
	for rfile in $game$ffix.$game
	do
		
		rm chunks.txt -f
		rm statesexp.txt -f
		rm states.txt -f
		rm startend.seconds -f
		rm startend.decisions -f
		
		c=1
		cp $rfile soar-game.script
		rfilename=${rfile%$suffix}
		
		echo "Version: " $rfilename
		java SentencesToSoar soar-game.script
		cp soar-game.soar ../language-comprehension/comprehension/test-sentences/
		while [ $c -le $repeat ]
		do
			echo $c
			c=`expr $c + 1`
			../../../soar/out/./soar -s game-data-agent.soar stop > out.txt 
		done
						
		#cp chunks.txt $stored$rfile$chunks
		diff -q chunks.txt $stored$rfile$chunks
		#diff -s chunks.txt $stored$rfile$chunks	
	done
done

declare -a grr=("tictactoe" "kconnect3" "aconnect3" "econnect4" "risk" "president" "crazy8" "ubreakthrough3" "9holes" "picaria" "mens3" )
#"othello") different betweenversion than stacked frogs need to teach
#broken "ubreakthrough" only 1 version,retrydue  "mens3" # othello

## now loop through the above array
for game in "${grr[@]}"
do
	echo "Testing game: " $game
	
	for rfile in *.$game
	do
		
		rm chunks.txt -f
		rm statesexp.txt -f
		rm states.txt -f
		rm startend.seconds -f
		rm startend.decisions -f
		
		c=1
		cp $rfile soar-game.script
		rfilename=${rfile%$suffix}
		
		echo "Version: " $rfilename
		java SentencesToSoar soar-game.script
		
		cp soar-game.soar ../language-comprehension/comprehension/test-sentences/
		while [ $c -le $repeat ]
		do
			echo $c
			c=`expr $c + 1`
			../../../soar/out/./soar -s game-data-agent.soar stop > out.txt
		done
						
		#cp chunks.txt $stored$rfile$chunks
		diff -q chunks.txt $stored$rfile$chunks
		#diff -s chunks.txt $stored$rfile$chunks	
	done
done
