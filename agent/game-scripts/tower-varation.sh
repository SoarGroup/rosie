#!/bin/bash

states=".states"
dec=".decisions"

repeat=99
no="no"
dot="."
a="a"
d="d"
game="tower3var"				

c=1
rm out.txt -f
x=0
while [ $x -le $repeat ]
do
	for rfile in tower3var.tower3var
	do
		cp $rfile soar-game.script
		
		printf "\nload " >> soar-game.script
		printf "final-tower3v$x." >> soar-game.script
		printf "\ndo you see the action of tower-of-hanoi-3?\n" >> soar-game.script
		#BUILD VERSION 2
		cp $ROSIE_PROJ/rosie/agent/game-scripts/soar-game.script $ROSIE_HOME/test-agents/game/example.sentences
		/home/jrkirk/rosie2/scripts/build_agent game >> out.txt
		
	  
		rm startend.seconds -f
		rm startend.decisions -f
		rm states.txt -f
		rm statesexp.txt -f
		
		
		##../../../soar/out/./soar -s game-agent.soar stop > out.txt
		rm rosieout.txt -f
		java edu.umich.rosie.gui.BasicRosieGUI /home/jrkirk/rosie2/rosie-project/rosie/test-agents/game/agent/rosie.game.config >> rosieout.txt
		rm actiontower3v$x.result -f
		tail rosieout.txt -n 1 >> actiontower3v$x.result
				
	done
	x=`expr $x + 1`
done
