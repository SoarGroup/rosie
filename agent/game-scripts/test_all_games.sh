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
solution="solution"
statesexp=".statesexp"
repeat=100
no="no"
dot="."
temp="temp"
a="a"
old="Old"
rm pertaskstats.txt -f
rm smemuse.txt -f
rm dialog.txt -f
rm script.txt -f
rm dialog.html -f
rm out.txt -f

#change into python with structure having all

declare -a arr=("agbfox" "Atower5" "blocksworld" "Blocksworld" "colorken" "Cannibals" "dsokoban" "Dshuffle" "ektour" "E8men" "frog" "Fifteenpuzzle" "golf" "Gkingtour" "husbands" "Holes9" "ijigsawdoku" "I8puzzle" "jmahjong" "Jsujiko" "kenken" "Kstackedfrogs" "logi5" "Lazystackedfrogs" "mapcolor" "Mens3" "nsudoku" "Nbishops" "othello" "Osuko" "pyramid" "Picaria" "queens" "rtravelsales" "Rsurvo" "stackedfrogs" "Swap" "tripeaks" "Tictactoe" "usolitaireR" "Ukakuro" "vsolitaire" "Vconnect4" "wives" "Wconnect3" "xsudoku" "Xbreakthrough" "ysorting" "zmaze" "Zmanager" "0nknights" "1nrooks" "2pushmaze" "3tower" "4tower" "5puzzle" "6nkings" "7familycross" "8puzzle" "9ksideswap")

declare -a gamedir=("fox-goose-bean" "tower-of-hanoi-5" "blocks-world" "blocks-world2" "kenken" "missionaries-and-cannibals" "sokoban" "shuffle" "knights-tour" "8-men-on-a-raft" "frogs-and-toads" "fifteen-puzzle" "golf-solitaire" "king-tour" "jealous-husbands" "nine-holes" "jigsawdoku" "eight-puzzle2" "mahjong" "sujiko" "kenken2" "king-stacking-frogs" "logi-five" "lazy-stacking-frogs" "four-map-coloring" "three-mens-morris" "sudoku" "n-bishops" "mini-othello" "suko" "pyramid-solitaire" "picaria" "n-queens" "traveling-salesman" "survo" "stacking-frogs" "4-corner-knights" "tri-peaks-solitaire" "tic-tac-toe" "peg-solitaire" "kakuro" "peg-solitaire2" "connect-four" "jealous-wives" "connect-three" "killer-sudoku" "breakthrough" "sorting" "simple-maze" "manager-actor" "n-knights" "n-rooks" "pushing-maze" "tower-of-hanoi" "tower-of-hanoi-4" "five-puzzle" "n-kings" "family-crossing" "eight-puzzle" "knight-swapping")
#dumb hacky solution
declare -a solves=(1 0 1 1 1 1 1 1 1 1 1 1 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 0 1 1 0 1 1 1 1 1 1 0 1 1 1 0 1 0 1 0 1 1 1 1 1 1 1 0 1 1 0 1 0)	
declare -a skips=(0 1 0 0 0 0 1 0 0 1 1 0 0 1 1 0 0 0 0 0 0 0 0 0 0 0 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 0 1)	
#declare -a skips=(1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 0 1)	
	
#add timestamp for data files
#make options, maybe shell args for solving or not, and even if transfer or not?, could do, might need to change how print files a bit
#no s1, need "gamename1"

#make so green yes no for passing test


for ((c=0; c<${#arr[@]}; ++c)); do
	game=${arr[c]}
	dir=${gamedir[c]}
	solv=${solves[c]}
	skip=${skips[c]}
	
	if [ $skip -eq 1 ]
	then
		continue
	fi
	#maintain for multiversions?
	#for rfile in $game.$game
	#for rfile in Xneworder*.$game
	for rfile in $game$test
	do
		rm out.txt -f
		#if [[ $rfile == *$no.$game* ]]; then
		#	continue
		#fi
		##if [[ $rfile == *$d.$game* ]]; then
	   	##	continue
		##fi
				
		d=1
		echo $game
		echo $dir
		python generatescript.py $game
		#cp $rfile soar-game.script
		
		cp $ROSIE_PROJ/rosie/agent/game-scripts/soar-game.script $ROSIE_HOME/test-agents/game/example.sentences
		/home/jrkirk/rosie2/scripts/build_agent game >> out.txt

		#OLD BUILD VERSION
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
			
			#sleep 1
			#sed -i '/^$/d' dialog.txt
			
			#python generatehtml.py $dir
			#cp dialog.html learning-data/*/$dir/
			#cp dialog.txt learning-data/*/$dir/
			cp soar-game.script script.txt
			cp script.txt learning-data/*/$dir/
			
			
			if [ $solv -eq 0 ]
			then
				continue
			fi
			 
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
				cp $rfile$sol $solution$dot$numsol
				cp $solution$dot$numsol learning-data/*/$dir/
			fi
			
			
			#print out time, states searched
			#cp s1.txt $rfile$top
			echo "Learning/Solution time (seconds):"
			more startend.seconds
			echo "States explored:"
			more states.txt
			
			###backup old benchmark
			###cp $rfile$tstats $rfile$tstats$old
			###cp $rfile$dec $rfile$dec$old
			###cp $rfile$states $rfile$states$old
			###cp $rfile$chunks $rfile$chunks$old
		   
			#if [ ! -f $rfile$tstats ]
			#then
			#	echo "  ::: COPY New time results :::   "
			#	cp startend.seconds $rfile$tstats
			#	cp startend.decisions $rfile$dec
			#	cp states.txt $rfile$states
			#	cp chunks.txt $rfile$chunks
			#fi
			#gettime code here
			

			#store benchmark
			cp startend.seconds learning-time.txt
			cp startend.decisions decisions.txt
			cp states.txt search-states.txt
			cp chunks.txt learned-chunks.txt
			
			cp learning-time.txt learning-data/*/$dir/
			cp decisions.txt learning-data/*/$dir/
			cp search-states.txt learning-data/*/$dir/
			cp learned-chunks.txt learning-data/*/$dir/
			
			
			#cat chunks.txt >> allchunks.txt
			#compare against benchmark timing
			#python compareSolutionTime.py $rfile
			
		done
	done
done
