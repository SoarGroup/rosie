#!/bin/bash

tstats=".tstats"
states=".states"
top=".top"
dec=".decisions"
sol=".solution"
stored="stored-"
statesexp=".statesexp"
repeat=4
no="no"
dot="."
a="a"
d="d"

#declare -a arr=("8puzzle5")
declare -a arr=("cannibals" "15ipuzzle" "3tower" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "lfamilycross" "jmahjong" "husbands")

#modify less slow cannibals gbfox husbands 15ipuzzle
#slow: x5tower 4tower
#broken: frog, solitaire, 2pushmaze dsokoban 

c=1
rm out.txt
for game in "${arr[@]}"
do
	#for rfile in $game.$game
	for rfile in *.$game
	do
		if [[ $rfile == *$no.$game* ]]; then
			#echo skip1
			continue
		fi
		
		if [[ $rfile == *$d.$game* ]]; then
			#echo skip2
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
			../../../../soar/out/./soar -s game-data-agent.soar stop > out.txt
			python calculateTeachSolvetime.py
			python calculateStatesExplored.py
		
			
			python soar-strip.py $rfile
			
			numsol=1
			###cp $rfile$sol $stored$game$sol$dot$numsol
			
			newfile=$stored$game$sol$dot$numsol
			
			while [ -f $stored$game$sol$dot$numsol ]
			do
				newfile=$stored$game$sol$dot$numsol
				if (diff -q $rfile$sol $newfile)
				then
					break
				else
					numsol=`expr $numsol + 1`
					
				fi
			done
			
			diff -s $rfile$sol $newfile
			
			#print out time, states searched
			cp s1.txt $rfile$top
			echo "Learning/Solution time (seconds):"
			more startend.seconds
			echo "States explored:"
			more states.txt
			
			#compare against benchmark timing
			#store benchmark
			#cp startend.seconds $rfile$tstats
			#cp startend.decisions $rfile$dec
			#cp states.txt $rfile$states
			python compareSolutionTime.py $rfile
			
		done
				
		#todo add strip out all but number and then do states comparison notdiff
		
		
		#store benchmark
		
	done
done
