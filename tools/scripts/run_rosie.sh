#!/bin/bash

AGENT_NAME=""
JAVA_CLASS=edu.umich.rosie.gui.BasicRosieGUI

# Run in background
BG="FALSE"

# Iterate over each argument given
while [[ $# -gt 0 ]]
do
	arg="$1"
	case $arg in
		-c|--cli)
			JAVA_CLASS=edu.umich.rosie.RosieCLI
			;;
		-m|--mobile)
			JAVA_CLASS=soargroup.rosie.RosieGUI
			;;
		-b|--background)
			BG="TRUE"
			;;
		*)
			AGENT_NAME="$1"
			;;
	esac
	shift
done

if [ "$AGENT_NAME" == "" ]; then 
	echo "Missing agent name"
	exit 1
fi

cd $ROSIE_HOME/test-agents/$AGENT_NAME/agent
if [ "$BG" == "TRUE" ]; then
	java $ROSIE_JAVA_FLAGS $JAVA_CLASS rosie.$AGENT_NAME.config &
else
	java $ROSIE_JAVA_FLAGS $JAVA_CLASS rosie.$AGENT_NAME.config
fi
