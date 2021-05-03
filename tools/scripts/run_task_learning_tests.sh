#!/bin/bash

TEST_LANG="python"
# The list of tests that are possible to run
FULL_TEST_LIST=(prim-actions kitchen mobile serve maintenance conditionals guide modifiers)

TESTS_TO_RUN=()
BUILD_ONLY=0
VERBOSE=""

# Iterate over each argument given
while [[ $# -gt 0 ]]
do
	arg="$1"
	case $arg in
		-h|--help)
			echo "run_task_learning_tests.sh:"
			echo "  -h|--help  prints help"
			echo "  -j|--java  runs the tests using java (default is python)"
			echo "  -l|--list  lists all runnable tests"
			echo "  -b|--build  just build the agents, without running them"
			echo "  -v|--verbose print the dialog while the agent is running"
			echo "By default, it runs all the tests"
			echo "  but you can specify only the tests you want:"
			echo "  run_task_learning_tests kitchen"
			exit 0
			;;
		-j|--java)
			TEST_LANG="java"
			;;
		-b|--build)
			BUILD_ONLY=1
			;;
		-v|--verbose)
			VERBOSE="-v"
			;;
		-l|--list)
			echo ${FULL_TEST_LIST[*]}
			exit 0
			;;
		*)
			TESTS_TO_RUN+=("$arg")
			;;
	esac
	shift
done

# If no tests are specified, run them all
if [[ ${#TESTS_TO_RUN} -eq 0 ]]; then
	TESTS_TO_RUN=${FULL_TEST_LIST[*]}
fi

# Run each test
for test in	${TESTS_TO_RUN[*]}; do
	# First check to make sure each tests is in the list of valid tests
	is_valid="false"
	for valid_test in ${FULL_TEST_LIST[*]}; do
		if [[ $test == $valid_test ]]; then
			is_valid="true"
			break
		fi
	done
	if [[ $is_valid == "false" ]]; then
		echo "ERROR: test $test is not a valid test"
		echo ""
		continue
	fi

	echo "################### TEST $test  #####################"
	cd $ROSIE_HOME/test-agents/task-tests/$test
	if [[ $VERBOSE == "-v" ]]; then
		echo "Running RosieAgentConfigurator"
	fi
	java edu.umich.rosie.tools.config.RosieAgentConfigurator $test.config -s

	if [[ $BUILD_ONLY -eq 1 ]]; then
		continue
	fi

	if [ "$TEST_LANG" == "python" ]; then
		echo "Running $test using python"
		python3 -m rosie.testing.run_rosie_test agent/rosie-client.config test-output.txt $VERBOSE
	else
		echo "Running $test using java"
		java edu.umich.rosie.RosieCLI agent/rosie-client.config
	fi

	if [[ $VERBOSE == "-v" ]]; then
		echo ""
		echo "Comparing output for test $test"
	fi

	echo "--------------------- DIFF START ------------------------ "
	diff test-output.txt correct-output.txt
	echo "---------------------- DIFF END ------------------------- "
	echo ""
done

