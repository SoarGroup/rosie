#!/bin/bash

cp $ROSIE_PROJ/rosie/agent/game-scripts/soar-game.script $ROSIE_HOME/test-agents/game/example.sentences

build_agent game

sh run2.sh
