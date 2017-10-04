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
old="Old"
#3tower
#
declare -a arr=("jmahjong")
# "stackedfrogs2" "lazystackedfrogs" "lazystackedfrogs2")
#declare -a arr=("cannibals" "15ipuzzle" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "lfamilycross" "jmahjong" "husbands" "3tower" "sudoku" "logi5" "jigsawdoku")
#solutions for these

#declare -a arr=("15ipuzzle" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "lfamilycross" 

#declare -a arr=("husbands" "3tower" "stackedfrogs" "stackedfrogs2" "lazystackedfrogs" "lazystackedfrogs2" "kstackedfrogs" "sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting")

#declare -a arr=("ken" "kenp" "ktour" "sudoku")

#declare -a arr=("stackedfrogs" "lazystackedfrogs" "kstackedfrogs")

#declare -a arr=("15ipuzzle" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "husbands" "3tower" "sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting")

#declare -a arr=("15ipuzzle" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "lfamilycross" "husbands" "3tower" "stackedfrogs" "stackedfrogs2" "lazystackedfrogs" "lazystackedfrogs2" "kstackedfrogs" "sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting")

#declare -a arr=("kstackedfrogs" "sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting")

#declare -a arr=("sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting")

#declare -a arr=("15ipuzzle" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "husbands" "wives" "lfamilycross" "3tower" "stackedfrogs" "lazystackedfrogs" "kstackedfrogs" "sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting" "jmahjong" "mapncolor3" "mapncolorv" "mapncolore" "mapncolor")

#declare -a arr=("sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku")
#declare -a arr=("3tower")
#declare -a arr=("sudoku" "logi5" "jigsawdoku" "cannibals")
#modify less slow cannibals gbfox husbands 15ipuzzle dsokoban2
#slow: x5tower 4tower
#broken old version frog, oldfrog, fixed solitaire, dsoko, 2pushmaze, frog3

c=1

for game in "${arr[@]}"
do
	#for rfile in ktourd.$game
	for rfile in *.$game
	do
		rm out.txt -f
		if [[ $rfile == *$no.$game* ]]; then
			continue
		fi
		#if [[ $rfile == *$d.$game* ]]; then
	   	#	continue
		#fi
				
		c=1
		cp $rfile soar-game.script
		
		#BUILD VERSION 2
		cp $ROSIE_PROJ/rosie/agent/game-scripts/soar-game.script $ROSIE_HOME/test-agents/game/example.sentences
		/home/jrkirk/rosie2/scripts/build_agent game >> out.txt

		#BUILD VERSION 1
		#java SentencesToSoar soar-game.script
		#cp soar-game.soar ../language-comprehension/comprehension/test-sentences/
		
		while [ $c -le $repeat ]
		do
			echo "Version: " $rfile " run " $c
			rm startend.seconds -f
			rm startend.decisions -f
			rm states.txt -f
			rm statesexp.txt -f

			c=`expr $c + 1`
			
			../../../soar/out/./soar -s game-agent.soar stop > out.txt
			##../../../soar/out/./soar -s game-data-agent.soar stop > out.txt
			python calculateTeachSolvetime.py
			python calculateStatesExplored.py
			
			
			python soar-strip.py $rfile
			
			numsol=1
			
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
			if [[ $foundsol < 1 ]];
			then
				cp $rfile$sol $stored$game$sol$dot$numsol
			fi
			
			
			#print out time, states searched
			cp s1.txt $rfile$top
			echo "Learning/Solution time (seconds):"
			more startend.seconds
			echo "States explored:"
			more states.txt
			
			##backup old benchmark
			##cp $rfile$tstats $rfile$tstats$old
			##cp $rfile$dec $rfile$dec$old
			##cp $rfile$states $rfile$states$old
			##cp $rfile$chunks $rfile$chunks$old
			
			if [ ! -f $rfile$tstats ]
			then
				echo "  ::: COPY New time results :::   "
				cp startend.seconds $rfile$tstats
				cp startend.decisions $rfile$dec
				cp states.txt $rfile$states
				cp chunks.txt $rfile$chunks
			fi
			#store benchmark
			#cp startend.seconds $rfile$tstats
			#cp startend.decisions $rfile$dec
			#cp states.txt $rfile$states
			#cp chunks.txt $rfile$chunks
			
			#cat chunks.txt >> allchunks.txt
			#compare against benchmark timing
			python compareSolutionTime.py $rfile
			
		done
	done
done
