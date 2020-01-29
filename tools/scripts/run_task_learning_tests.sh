#!/bin/bash

TEST_LANG="python"

cd $ROSIE_HOME/test-agents/task-tests

# Iterate over each argument given
while [[ $# -gt 0 ]]
do
	arg="$1"
	case $arg in
		-j|--java)
			TEST_LANG="java"
			;;
		*)
			;;
	esac
	shift
done

# Testing Primitive Actions:
echo "################### RUNNING prim-actions ####################"
java edu.umich.rosie.tools.config.RosieAgentConfigurator prim-actions.config -s

if [ "$TEST_LANG" == "python" ]; then
	python3 -m rosie.testing prim-actions
else
	java edu.umich.rosie.RosieCLI prim-actions/agent/rosie.prim-actions.config
fi

echo "### OUTPUT ERRORS for prim-actions: "
diff output_prim-actions.txt prim-actions/correct-output.txt
echo "### END TEST ###"


# Test 1:
echo "################### RUNNING test1 ####################"
java edu.umich.rosie.tools.config.RosieAgentConfigurator test1.config -s

if [ "$TEST_LANG" == "python" ]; then
	python3 -m rosie.testing test1
else
	java edu.umich.rosie.RosieCLI test1/agent/rosie.test1.config
fi

echo "### OUTPUT ERRORS for test1: "
diff output_test1.txt test1/correct-output.txt
echo "### END TEST ###"

