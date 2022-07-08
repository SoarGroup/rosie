#!/bin/bash

cp $ROSIE_PROJ/rosie/agent/game-scripts/soar-game.script $ROSIE_HOME/test-agents/chat-rosie/chat-rosie.sentences

./build_agent chat-rosie

#sh run2.sh
