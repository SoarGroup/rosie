#!/bin/bash

chunks=".chunks"
tstats=".tstats"
states=".states"
top=".top"
dec=".decisions"
sol=".solution"
stored="stored-"
statesexp=".statesexp"
repeat=1
no="no"
dot="."
a="a"
d="d"
#3tower
#
#declare -a arr=("3tower")
# "stackedfrogs2" "lazystackedfrogs" "lazystackedfrogs2")
#declare -a arr=("cannibals" "15ipuzzle" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "lfamilycross" "jmahjong" "husbands" "3tower" "sudoku" "logi5" "jigsawdoku")
#solutions for these
declare -a arr=("15ipuzzle" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "lfamilycross" "husbands" "3tower" "stackedfrogs" "stackedfrogs2" "lazystackedfrogs" "lazystackedfrogs2" "kstackedfrogs")
#modify less slow cannibals gbfox husbands 15ipuzzle
#slow: x5tower 4tower
#broken: frog, solitaire, 2pushmaze dsokoban 

c=1
rm out.txt -f
for game in "${arr[@]}"
do
	#for rfile in $game.$game
	for rfile in *.$game
	do
		if [[ $rfile == *$no.$game* ]]; then
			continue
		fi
		if [[ $rfile == *$d.$game* ]]; then
			continue
		fi
				
		c=1
		cp $rfile soar-game.script
		
		
		java SentencesToSoar soar-game.script
		cp soar-game.soar ../language-comprehension/comprehension/test-sentences/
		while [ $c -le $repeat ]
		do
			echo "Version: " $rfile " run " $c
			rm startend.seconds -f
			rm startend.decisions -f
			rm states.txt -f
			rm statesexp.txt -f

			c=`expr $c + 1`
			../../../../soar/out/./soar -s game-agent.soar stop > out.txt
			python calculateTeachSolvetime.py
			python calculateStatesExplored.py
		
			
			python soar-strip.py $rfile
			
			numsol=1
			##cp $rfile$sol $stored$game$sol$dot$numsol
			
			newfile=$stored$game$sol$dot$numsol
			foundsol=0
			while [ -f $stored$game$sol$dot$numsol ]
			do
				newfile=$stored$game$sol$dot$numsol
				if (diff -q $rfile$sol $newfile)
				then
					foundsol=1
					break
				else
					numsol=`expr $numsol + 1`
				fi
			done

			diff -s $rfile$sol $newfile
			#if [[ $foundsol < 1 ]];
			#then
			#	cp $rfile$sol $stored$game$sol$dot$numsol
			#fi
			
			
			#print out time, states searched
			cp s1.txt $rfile$top
			echo "Learning/Solution time (seconds):"
			more startend.seconds
			echo "States explored:"
			more states.txt
			
			#store benchmark
			#cp startend.seconds $rfile$tstats
			#cp startend.decisions $rfile$dec
			#cp states.txt $rfile$states
			#cp chunks.txt $rfile$chunks
			
			#compare against benchmark timing
			python compareSolutionTime.py $rfile
			
		done
	done
done
