#!/bin/bash

cd $ROSIE_HOME/test-agents/task-tests

# Test 1:
echo "################### RUNNING test1 ####################"
java edu.umich.rosie.tools.config.RosieAgentConfigurator test1.config -s
python3 -m rosie.testing test1
echo "### OUTPUT ERRORS for test1: "
diff test-output.txt test1/correct-output.txt
echo "### END TEST ###"

