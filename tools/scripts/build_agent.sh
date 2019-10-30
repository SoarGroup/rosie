#!/bin/bash

cd $ROSIE_HOME/test-agents/$1
java edu.umich.rosie.tools.config.RosieAgentConfigurator $1.config
