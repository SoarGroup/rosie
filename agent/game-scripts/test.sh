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
repeat=1
no="no"
dot="."
temp="temp"
a="a"
old="Old"

#change into python with structure having all
declare -a arr=("agbfox" "Atower5" "blocksworld" "Blocksworld" "colorken" "Cannibals" "dsokoban" "Dshuffle" "ektour" "E8men" "frog" "Fifteenpuzzle" "golf" "Gkingtour" "husbands" "Holes9" "ijigsawdoku" "I8puzzle" "jmahjong" "Jsujiko" "kenken" "Kstackedfrogs" "logi5" "Lazystackedfrogs" "mapcolor" "Mens3" "nsudoku" "Nbishops" "othello" "Osuko" "pyramid" "Picaria" "queens" "rtravelsales" "Rsurvo" "stackedfrogs" "Swap" "tripeaks" "Tictactoe" "usolitaireR" "Ukakuro" "vsolitaire" "Vconnect4" "wives" "Wconnect3" "xsudoku" "Xbreakthrough" "ysorting" "zmaze" "Zmanager" "0nknights" "1nrooks" "2pushmaze" "3tower" "4tower" "5puzzle" "6nkings" "7familycross" "8puzzle" "9ksideswap")

declare -a gamedir=("fox-goose-bean" "tower-of-hanoi-5" "blocks-world" "blocks-world2" "kenken" "missionaries-and-cannibals" "sokoban" "shuffle" "knights-tour" "8-men-on-a-raft" "frogs-and-toads" "fifteen-puzzle" "golf-solitaire" "king-tour" "jealous-husbands" "nine-holes" "jigsawdoku" "eight-puzzle2" "mahjong" "sujiko" "kenken2" "king-stacking-frogs" "logi-five" "lazy-stacking-frogs" "four-map-coloring" "three-mens-morris" "sudoku" "n-bishops" "mini-othello" "suko" "pyramid-solitaire" "picaria" "n-queens" "traveling-salesman" "survo" "stacking-frogs" "4-corner-knights" "tri-peaks-solitaire" "tic-tac-toe" "peg-solitaire" "kakuro" "peg-solitaire2" "connect-four" "jealous-wives" "connect-three" "killer-sudoku" "breakthrough" "sorting" "simple-maze" "manager-actor" "n-knights" "n-rooks" "pushing-maze" "tower-of-hanoi" "tower-of-hanoi-4" "five-puzzle" "n-kings" "family-crossing" "eight-puzzle" "knight-swapping")

#dumb hacky solution
declare -a solves=(1 0 1 1 1 1 1 1 1 1 1 1 1 1 1 0 1 1 1 1 1 1 1 1 1 0 1 1 0 1 1 0 1 1 1 1 1 1 0 1 1 1 0 1 0 1 0 1 1 1 1 1 1 1 0 1 1 0 1 0)	
#declare -a skips=(0 1 0 0 0 0 1 0 0 1 1 0 0 1 1 0 0 0 0 0 0 0 0 0 0 0 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 0 1)	

#declare -a skips=(0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0)	
declare -a skips=(0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0)	

#declare -a skips=(1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 1 1 1) 

#declare -a skips=(1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 1 1 1) 

#declare -a skips=(1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1) 
#declare -a skips=(1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 1 1 1 1 1 1 1 1) 
#declare -a skips=(1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 0 1)	
	
#add timestamp for data files
#make options, maybe shell args for solving or not, and even if transfer or not?, could do, might need to change how print files a bit
#no s1, need "gamename1"
#make so green yes no for passing test

echo ${#arr[@]}

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
	for rfile in $game$test
	do
		rm out.txt -f
		#if [[ $rfile == *$no.$game* ]]; then
						
		d=1
		echo $game
		echo $dir
		python generatescript.py $game
				
		cp $ROSIE_PROJ/rosie/agent/game-scripts/soar-game.script $ROSIE_HOME/test-agents/game/example.sentences
		/home/jrkirk/rosie2/scripts/build_agent game >> out.txt
				
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
			rm parse.txt -f
			
			d=`expr $d + 1`
			
			#sh javarun.sh > dialog.txt
			$SOAR_HOME/./soar -s game-agent.soar halt > out.txt
						
			#sleep 1
			#sed -i '/^$/d' dialog.txt
			
			#python generatehtml.py $dir
			#cp dialog.html learning-data/*/$dir/
			#cp dialog.txt learning-data/*/$dir/
			
			
			cp soar-game.script script.txt
			#add#cp script.txt learning-data/*/$dir/
			
			cp chunks.txt learned-chunks.txt
			
			#add#cp learned-chunks.txt learning-data/*/$dir/
			#add#cp parse.txt learning-data/*/$dir/
			
			if [ $solv -eq 0 ]
			then
				continue
			fi
			

			sed -i "s|affordance1 grabbable1|arm-status not-grabbed|g" solution.txt
			sed -i "s|is-grabbed1 not-grabbed1|is-grabbable1 grabbable1|g" solution.txt

			python calculateTeachSolvetime.py
			python calculateStatesExplored.py
			
			
			python soar-strip.py $rfile
			
			numsol=1
			
			
			#newfile=learning-data/*/$dir/$solution$dot$numsol
			#newfile=$stored$game$sol$dot$numsol
			foundsol=0
			#while [ -f $stored$game$sol$dot$numsol ]
			while [ -f learning-data/*/$dir/$solution$dot$numsol ]
			do
				#newfile=learning-data/*/$dir/$solution$dot$numsol
				#newfile=$stored$game$sol$dot$numsol
				if (diff -q $rfile$sol learning-data/*/$dir/$solution$dot$numsol)
				then
					foundsol=1
					break
				else
					numsol=`expr $numsol + 1`
				fi
			done

			diff -s $rfile$sol learning-data/*/$dir/$solution$dot$numsol
			if [[ $foundsol < 1 ]];
			then
				cp $rfile$sol $solution$dot$numsol
				#addcp $solution$dot$numsol learning-data/*/$dir/
				rm $solution$dot$numsol
			fi

			#rm $rfile$sol 
						
			#print out time, states searched
			echo "Learning/Solution time (seconds):"
			more startend.seconds
			echo "States explored:"
			more states.txt
								   
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
			
			#add#cp learning-time.txt learning-data/*/$dir/
			#add#cp decisions.txt learning-data/*/$dir/
			#add#cp search-states.txt learning-data/*/$dir/
			
			#compare against benchmark timing
			#python compareSolutionTime.py $rfile
			
		done
		
	done
	#break
done
