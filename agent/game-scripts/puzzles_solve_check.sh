#!/bin/bash

chunks=".chunks"
tstats=".tstats"
states=".states"
top=".top"
dec=".decisions"
sol=".solution"
stored="stored-"
statesexp=".statesexp"
repeat=200
no="no"
dot="."
temp="temp"
a="a"
d="d"
old="Old"

#3tower
#
#declare -a arr=("cannibals")
#declare -a arr=("nknights")
#declare -a arr=("nkings")
#declare -a arr=("kakuro" "ksideswap4")
#declare -a arr=("shuffle")
#"kenp" "colorkenp")
#
#
#"logi5" "jigsawdoku" "sudoku" "mapncolor3")
#jmahjong random error
#declare -a arr=("sudoku" "frog" "nrooks") #rand seed seg faults all work on #25!
#on 25 all these working pre removal of input-object
#and post now try deep copy sentence

#declare -a arr=("sorting" "gbfox" "worldblocks" "blocksworld" "zmaze" "nknights" "nkings" "kswap1" "ktour" "travelsales" "nrooks" "lfamilycross")
#declare -a arr=("nrooks" "lfamilycross")
#declare -a arr=("gbfox" "worldblocks" "blocksworld" "zmaze" "nknights" "nkings" "kswap1" "ktour" "travelsales" "nrooks" "lfamilycross")

#all working but family cross 9/1/2018
#3 betweens not working, but everything else 9/2
#FIXED survo,kakuro broken
#incorrect tripeaks golf 

#2pushmaze problem with two blue things retrieving two smem..fixed
#9/6 stable on all but lfamilycross, tripeaks golf still incorrect 
#fixed survo, kakuro
declare -a arr=("2pushmazeant2") #ant
#declare -a arr=("kakuro" "survo")

#lazystackedfrogs generates bad solution maybe because of between exclusive?
#^yes!!, and doesnt learn between inclusive?
#only for lazy or for normal too?
#hmmm yes dont' have problem for stackedfrogs, only detects inclusive strange
#bad solutions lazystackedfrogs,golf, tripeaks
#declare -a arr=("solitaireR" "jmahjongR") #check solution
#working!
#declare -a arr=(
#maybe broken:
# "kakuro" "nqueens" 
#declare -a arr=("husbands" "wives" "logi5" "jigsawdoku" "sudoku" "ksudoku" "frog" "2pushmaze" "dsokoban2" "solitaire" "solitaireR" "jmahjongR" "mapncolor3" "survo" "shuffle" "8puzzle" "yiso5puzzle" "iso8puzzle" "5puzzle" "15ipuzzle" "3tower" "worldblocks" "blocksworld" "zmaze" "lazystackedfrogs" "colorken" "ken" "kenp" "colorkenp" "tripeaks" "golf" "suko" "sujiko" "jmahjong" "pyramid" "cannibals" "sorting" "gbfox" "nknights" "nkings" "kswap1" "ksideswap4" "ktour" "travelsales" "nrooks" "stackedfrogs")
#broken
#declare -a arr=("kakuro" "nqueens" 
#declare -a arr=("stackedfrogs2" "kstackedfrogs")

#
#other between
#declare -a arr=(
#"frog"

#declare -a arr=("logi5" "jigsawdoku" "sudoku" "ksudoku" "mapncolor3" "colorken" "ken" "kenp" "colorkenp" "tripeaks" "golf" "suko" "sujiko" "shuffle" "jmahjong" "pyramid" "husbands" "wives" "8puzzle" "yiso5puzzle" "iso8puzzle" "5puzzle" "3tower" "cannibals" "sorting" "gbfox" "worldblocks" "blocksworld" "zmaze" "nknights" "nkings" "nqueens" "kswap1" "ktour" "travelsales" "nrooks" "frog" "2pushmaze" "dsokoban2" "solitaire" "stackedfrogs" "stackedfrogs2" "kstackedfrogs" "lazystackedfrogs" "lfamilycross")


#"pyramid" "shuffle"
#"colorken" "colorkenp" "yiso5puzzle" "iso8puzzle" "2pushmaze" "jmahjongR" "solitaireR" "5puzzle" "8puzzle" "3tower" "cannibals" "sorting" "logi5" "jigsawdoku" "wives" "dsokoban2"
  
#declare -a arr=("husbands" "8puzzle" "gbfox" "sudoku" "zmaze" "kenp" "nknights" "nkings" "kakuro" "kswap2" "ksideswap4" "ktour" "simpleages" "travelsales" "nrooks" "tripeaks" "sujiko" "suko" "golf" "survo" "lfamilycross" "ken")
#declare -a arr=("nknights" "nkings" "kakuro" "kswap2" "ksideswap4"  "ktour" "simpleages" "travelsales" "nrooks" "tripeaks" "sujiko" "suko" "golf" "survo" "pyramid" "shuffle" "colorken" "colorkenp" "yiso5puzzle" "iso8puzzle" "2pushmaze" "jmahjongR" "solitaireR" "5puzzle" "8puzzle" "3tower" "cannibals" "sorting" "logi5" "jigsawdoku" "wives" "dsokoban2" "husbands" "8puzzle" "gbfox" "sudoku" "zmaze" "shuffle" "kenp" "lfamilycross" "ken")
#survo
#declare -a arr=("transfersudocolorkenshufflenosc4")
#all but jmahjong no remove works

#declare -a arr=("pyramid" "colorken" "colorkenp" "yiso5puzzle" "iso8puzzle" "2pushmaze" "jmahjongR" "solitaireR" "5puzzle" "8puzzle" "3tower" "cannibals" "sorting" "logi5" "jigsawdoku" "wives" "dsokoban2" "husbands" "8puzzle" "gbfox" "sudoku" "zmaze" "shuffle" "kenp" "lfamilycross" "ken")

#declare -a arr=("pyramid" "colorken" "colorkenp" "yiso5puzzle" "iso8puzzle" "2pushmaze" "jmahjong" "jmahjongR" "solitaire" "solitaireR" "5puzzle" "8puzzle" "3tower" "cannibals" "sorting" "logi5" "jigsawdoku" "wives" "dsokoban2" "husbands" "8puzzle" "gbfox" "sudoku" "zmaze" "shuffle" "kenp" "lfamilycross" "ken")


#declare -a arr=("shuffle" "colorken")
#declare -a arr=("yiso5puzzle" "iso8puzzle" "2pushmaze" "jmahjong" "jmahjongR" "solitaire" "solitaireR" "5puzzle" "8puzzle" "3tower" "cannibals" "sorting" "logi5" "jigsawdoku" "wives" "dsokoban2" "husbands" "8puzzle" "gbfox" "sudoku" "zmaze" "shuffle" "kenp" "lfamilycross" "ken")
#declare -a arr=("logi5" "jigsawdoku" "wives" "dsokoban2" "husbands" "8puzzle" "gbfox" "sudoku" "zmaze" "shuffle" "kenp" "lfamilycross")
#declare -a arr=("solitaire" "solitaireR" "kenp" "cannibals" "sorting" "colorken" "logi5" "jigsawdoku" "wives" "3tower" "dsokoban2" "husbands" "8puzzle" "gbfox" "sudoku" "zmaze" "lfamilycross")
#"solitaire")
#solitairR diff bad double remove solution
#soliatre different good solutions?
#FIXED ken in, sometime broken
#FIXED and sometime sudoku broken, and another solution for solitaire
#FIXED solitaireR random broken 23
#sudoku random segfault newordersubsame.sudoku

#declare -a arr=("ken" "3tower" "dsokoban2" "husbands" "8puzzle" "gbfox" "sudoku" "zmaze" "solitaire")
# "solitaireR")

# "stackedfrogs2" "lazystackedfrogs" "lazystackedfrogs2")
#declare -a arr=("cannibals" "15ipuzzle" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "lfamilycross" "jmahjong" "husbands" "3tower" "sudoku" "logi5" "jigsawdoku")
#solutions for these

#declare -a arr=("15ipuzzle" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "lfamilycross" 

#declare -a arr=("husbands" "3tower" "stackedfrogs" "stackedfrogs2" "lazystackedfrogs" "lazystackedfrogs2" "kstackedfrogs" "sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "OBksudoku" "ktour" "sorting")

#declare -a arr=("ken" "kenp" "ktour" "sudoku")

#declare -a arr=("stackedfrogs" "lazystackedfrogs" "kstackedfrogs")

#declare -a arr=("15ipuzzle" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "husbands" "3tower" "sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting")

#declare -a arr=("15ipuzzle" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "lfamilycross" "husbands" "3tower" "stackedfrogs" "stackedfrogs2" "lazystackedfrogs" "lazystackedfrogs2" "kstackedfrogs" "sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting")

#declare -a arr=("kstackedfrogs" "sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting")

#declare -a arr=("sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting")

#declare -a arr=("sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting" "jmahjong" "mapncolor3" "mapncolorv" "mapncolore" "mapncolor")

#declare -a arr=("15ipuzzle" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "husbands" "wives" "lfamilycross" "3tower" "stackedfrogs" "lazystackedfrogs" "kstackedfrogs" "sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting" "jmahjong" "mapncolor3" "mapncolorv" "mapncolore" "mapncolor")

#ktourprlrg.ktour  broken? only with random seed?
#QsolitaireR.solitaireR broken with random seed


#all ken.ken variants broken
#ksudoku broken
#jmahjongR broken
#managers broken
#stacked frogs broken?

#here
#declare -a arr=("15ipuzzle" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "husbands" "wives" "3tower" "sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "solitaireR" "dsokoban2" "2pushmaze" "frog3" "sorting" "jmahjong" "mapncolor3" "ktour" "lfamilycross")

#declare -a arr=("15ipuzzle" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "husbands" "wives" "3tower" "sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "solitaireR" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "sorting" "jmahjong" "mapncolor3" "ktour" "lfamilycross")

#slow ktour lfamily cross 
#broken gbfox husbands wives(assuming) lfamily(assuming) cannibals assuming fixed?
#declare -a arr=declare -a arr=("15ipuzzle" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "husbands" "wives" "lfamilycross" 


#declare -a arr=("3tower" "sudoku" "logi5" "jigsawdoku" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting" "jmahjong" "mapncolor3" "mapncolorv" "mapncolore" "mapncolor")

#declare -a arr=("sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting" "jmahjong" "mapncolor3" "mapncolorv" "mapncolore" "mapncolor")

#declare -a arr=("sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku")
#declare -a arr=("3tower")
#declare -a arr=("sudoku" "logi5" "jigsawdoku" "cannibals")
#modify less slow cannibals gbfox husbands 15ipuzzle dsokoban2
#slow: x5tower 4tower
#broken old version frog, oldfrog, fixed solitaire, dsoko, 2pushmaze, frog3

c=1
rm pertaskstats.txt -f
rm smemuse.txt -f

for game in "${arr[@]}"
do
	#for rfile in $game.$game
	for rfile in Xneworder*.$game
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
			rm solution.txt -f
			rm stats.txt -f
			rm endlearntime.txt -f

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
				cp $rfile$sol $stored$game$sol$dot$numsol$dot$temp
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
