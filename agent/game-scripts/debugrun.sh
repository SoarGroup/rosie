#!/bin/bash

#sh doit.sh > garbage
cp $ROSIE_PROJ/rosie/agent/game-scripts/soar-game.script $ROSIE_HOME/test-agents/game/example.sentences
/home/jrkirk/rosie2/scripts/build_agent game
#java SentencesToSoar soar-game.script 
#cp soar-game.soar ../language-comprehension/comprehension/test-sentences/
$SOAR_HOME/./soar -s game-agent.soar halt


