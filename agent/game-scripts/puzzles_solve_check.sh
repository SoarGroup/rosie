#!/bin/bash

dialog=".dialog"
txt=".txt"
test=".test"
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
temp="temp"
a="a"
d="d"
old="Old"

#declare -a arr=("kakuro" "ksideswap4")
#declare -a arr=("sudoku" "frog" "nrooks") #rand seed seg faults all work on #25!
#on 25 all these working pre removal of input-object
#and post now try deep copy sentence
#declare -a arr=("sorting" "gbfox" "worldblocks" "blocksworld" "zmaze" "nknights" "nkings" "kswap1" "ktour" "travelsales" "nrooks" "lfamilycross")
#all working but family cross 9/1/2018
#3 betweens not working, but everything else 9/2
#incorrect tripeaks golf 
#declare -a arr=("2pushmazeant2") #ant
#lazystackedfrogs generates bad solution maybe because of between exclusive?
#^yes!!, and doesnt learn between inclusive?
#only for lazy or for normal too?
#hmmm yes dont' have problem for stackedfrogs, only detects inclusive strange
#bad solutions lazystackedfrogs,golf, tripeaks

#simplages, simplerisk
#all but jmahjong no remove works #jmahjong random error
#solitairR diff bad double remove solution
#soliatre different good solutions?
#sudoku random segfault newordersubsame.sudoku
#solutions for these
#declare -a arr=("15ipuzzle" "gbfox" "8puzzle4" "8puzzle5" "8puzzle6" "8puzzle6alt" "8puzzle" "5puzzle" "iso8puzzle" "yiso5puzzle" "zmaze" "blocksworld" "worldblocks" "lfamilycross" "husbands" "3tower" "stackedfrogs" "stackedfrogs2" "lazystackedfrogs" "lazystackedfrogs2" "kstackedfrogs" "sudoku" "logi5" "jigsawdoku" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting")

#ktourprlrg.ktour QsolitaireR.solitaireR broken with random seed

#declare -a arr=("sudoku" "logi5" "jigsawdoku" "wives" "cannibals" "solitaire" "dsokoban2" "2pushmaze" "frog3" "ken" "colorken" "kenp" "ksudoku" "ktour" "sorting" "jmahjong" "mapncolor3" "mapncolorv" "mapncolore" "mapncolor")

#modify less slow cannibals gbfox husbands 15ipuzzle dsokoban2
#slow: x5tower 4tower
#broken old version frog, oldfrog, fixed solitaire, dsoko, 2pushmaze, frog3

c=1
rm pertaskstats.txt -f
rm smemuse.txt -f

rm dialog.txt -f
rm script.txt -f
rm dialog.html -f

declare -a arr=("agbfox" "Atower5" "blocksworld" "Blocksworld" "colorken" "Cannibals" "dsokoban" "Dshuffle" "ektour" "E8men" "frog" "Fifteenpuzzle" "golf" "Gkingtour" "husbands" "Holes9" "ijigsawdoku" "I8puzzle" "jmahjong" "Jsujiko" "kenken" "Kstackedfrogs" "logi5" "Lazystackedfrogs" "mapcolor" "Mens3" "nsudoku" "Nbishops" "othello" "Osuko" "pyramid" "Picaria" "queens" "rtravelsales" "Rsurvo" "stackedfrogs" "Swap" "tripeaks" "Tictactoe" "usolitaireR" "Ukakuro" "vsolitaire" "Vconnect4" "wives" "Wconnect3" "xsudoku" "Xbreakthrough" "ysorting" "zmaze" "Zmanager" "0nknights" "1nrooks" "2pushmaze" "3tower" "4tower" "5puzzle" "6nkings" "7familycross" "8puzzle" "9ksideswap")
declare -a gamedir=("fox-goose-bean" "tower-of-hanoi-5" "blocks-world" "blocks-world2" "kenken" "missionaries-and-cannibals" "sokoban" "shuffle" "knights-tour" "8-men-on-a-raft" "frogs-and-toads" "fifteen-puzzle" "golf-solitaire" "king-tour" "jealous-husbands" "nine-holes" "jigsawdoku" "eight-puzzle2" "mahjong" "sujiko" "kenken2" "king-stacking-frogs" "logi-five" "lazy-stacking-frogs" "four-map-coloring" "three-mens-morris" "sudoku" "n-bishops" "mini-othello" "suko" "pyramid-solitaire" "picaria" "n-queens" "traveling-salesman" "survo" "stacking-frogs" "4-corner-knights" "tri-peaks-solitaire" "tic-tac-toe" "peg-solitaire" "kakuro" "peg-solitaire2" "connect-four" "jealous-wives" "connect-three" "killer-sudoku" "breakthrough" "sorting" "simple-maze" "manager-actor" "n-knights" "n-rooks" "pushing-maze" "tower-of-hanoi" "tower-of-hanoi-4" "five-puzzle" "n-kings" "family-crossing" "eight-puzzle" "knight-swapping")
	
	
#add timestamp for data files

for ((c=0; c<${#arr[@]}; ++c)); do
	game=${arr[c]}
	dir=${gamedir[c]}
	
	#maintain for multiversions?
	#for rfile in $game.$game
	#for rfile in Xneworder*.$game
	for rfile in $game.$test
	do
		rm out.txt -f
		#if [[ $rfile == *$no.$game* ]]; then
		#	continue
		#fi
		##if [[ $rfile == *$d.$game* ]]; then
	   	##	continue
		##fi
				
		c=1
		echo $game
		echo $dir
		python generatescript.py $game
		#cp $rfile soar-game.script
		
				
		#BUILD VERSION 2
		cp $ROSIE_PROJ/rosie/agent/game-scripts/soar-game.script $ROSIE_HOME/test-agents/game/example.sentences
		/home/jrkirk/rosie2/scripts/build_agent game >> out.txt

		#BUILD VERSION 1
		#java SentencesToSoar soar-game.script
		#cp soar-game.soar ../language-comprehension/comprehension/test-sentences/
		
		while [ $d -le $repeat ]
		do
			echo "Version: " $rfile " run " $d
			rm startend.seconds -f
			rm startend.decisions -f
			rm states.txt -f
			rm statesexp.txt -f
			rm solution.txt -f
			rm stats.txt -f
			rm endlearntime.txt -f
			rm dialog.html -f
			rm dialog.txt -f
			rm script.txt -f
			rm statsm.txt -f
			rm pertaskstats.txt -f
			rm smemuse.txt -f
			rm statsdc.txt -f
			
			d=`expr $d + 1`
			
			#sh javarun.sh > dialog.txt
			$SOAR_HOME/./soar -s game-agent.soar halt > out.txt
			#../../../soar/out/./soar -s game-agent.soar stop > out.txt
			##../../../soar/out/./soar -s game-data-agent.soar stop > out.txt
			
			sleep 1
			sed -i '/^$/d' dialog.txt

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
			
			###backup old benchmark
			###cp $rfile$tstats $rfile$tstats$old
			###cp $rfile$dec $rfile$dec$old
			###cp $rfile$states $rfile$states$old
			###cp $rfile$chunks $rfile$chunks$old
		   
			if [ ! -f $rfile$tstats ]
			then
				echo "  ::: COPY New time results :::   "
				cp startend.seconds $rfile$tstats
				cp startend.decisions $rfile$dec
				cp states.txt $rfile$states
				cp chunks.txt $rfile$chunks
			fi
			#gettime code here
			#store benchmark
			cp startend.seconds $rfile$tstats
			cp startend.decisions $rfile$dec
			cp states.txt $rfile$states
			cp chunks.txt $rfile$chunks
			
			python generatehtml.py $dir
			cp dialog.html learning-data/*/$dir/
			cp dialog.txt learning-data/*/$dir/
			cp soar-game.script script.txt
			cp script.txt learning-data/*/$dir/
			

			#cat chunks.txt >> allchunks.txt
			#compare against benchmark timing
			python compareSolutionTime.py $rfile
			
		done
	done
done
