#!/bin/bash

cp $ROSIE_PROJ/rosie/agent/game-scripts/soar-game.script $ROSIE_HOME/test-agents/game/example.sentences

/home/jrkirk/rosie2/scripts/build_agent game

sh run2.sh
