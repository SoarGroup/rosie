dialog=".dialog"
test=".test"
txt=".txt"

declare -a arr=("agbfox" "Atower5" "blocksworld" "Blocksworld" "colorken" "Cannibals" "dsokoban" "Dshuffle" "ektour" "E8men" "frog" "Fifteenpuzzle" "golf" "Gkingtour" "husbands" "Holes9" "ijigsawdoku" "I8puzzle" "jmahjong" "Jsujiko" "kenken" "Kstackedfrogs" "logi5" "Lazystackedfrogs" "mapcolor" "Mens3" "nsudoku" "Nbishops" "othello" "Osuko" "pyramid" "Picaria" "queens" "rtravelsales" "Rsurvo" "stackedfrogs" "Swap" "tripeaks" "Tictactoe" "usolitaireR" "Ukakuro" "vsolitaire" "Vconnect4" "wives" "Wconnect3" "xsudoku" "Xbreakthrough" "ysorting" "zmaze" "Zmanager" "0nknights" "1nrooks" "2pushmaze" "3tower" "4tower" "5puzzle" "6nkings" "7familycross" "8puzzle" "9ksideswap")

declare -a gamedir=("fox-goose-bean" "tower-of-hanoi-5" "blocks-world" "blocks-world2" "kenken" "missionaries-and-cannibals" "sokoban" "shuffle" "knights-tour" "8-men-on-a-raft" "frogs-and-toads" "fifteen-puzzle" "golf-solitaire" "king-tour" "jealous-husbands" "nine-holes" "jigsawdoku" "eight-puzzle2" "mahjong" "sujiko" "kenken2" "king-stacking-frogs" "logi-five" "lazy-stacking-frogs" "four-map-coloring" "three-mens-morris" "sudoku" "n-bishops" "mini-othello" "suko" "pyramid-solitaire" "picaria" "n-queens" "traveling-salesman" "survo" "stacking-frogs" "4-corner-knights" "tri-peaks-solitaire" "tic-tac-toe" "peg-solitaire" "kakuro" "peg-solitaire2" "connect-four" "jealous-wives" "connect-three" "killer-sudoku" "breakthrough" "sorting" "simple-maze" "manager-actor" "n-knights" "n-rooks" "pushing-maze" "tower-of-hanoi" "tower-of-hanoi-4" "five-puzzle" "n-kings" "family-crossing" "eight-puzzle" "knight-swapping")
rm dialog.txt -f
rm script.txt -f
rm dialog.html -f

for ((c=0; c<${#arr[@]}; ++c)); do
	game=${arr[c]}
	dir=${gamedir[c]}
	echo $game
	echo $dir
	python generatescript.py $game
	
	cp $ROSIE_PROJ/rosie/agent/game-scripts/soar-game.script $ROSIE_HOME/test-agents/game/example.sentences
	/home/jrkirk/rosie2/scripts/build_agent game > out.txt
		
	sh javarun.sh > dialog.txt
	#$SOAR_HOME/./soar -s game-agent.soar halt
	
	sleep 1
	sed -i '/^$/d' dialog.txt
	
	python generatehtml.py $dir
	
	cp dialog.html learning-data/*/$dir/
	cp dialog.txt learning-data/*/$dir/
	cp soar-game.script script.txt
	cp script.txt learning-data/*/$dir/
	
	rm dialog.html -f
	rm dialog.txt -f
	rm script.txt -f
	rm statsm.txt -f
	rm pertaskstats.txt -f
	rm smemuse.txt -f
	rm statsdc.txt -f
done
