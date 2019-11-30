#!/bin/bash

cd $ROSIE_HOME/test-agents/$1

if [ $# -eq 1 ]; then
	java edu.umich.rosie.tools.config.RosieAgentConfigurator $1.config
else
	java edu.umich.rosie.tools.config.RosieAgentConfigurator $2.config
fi
