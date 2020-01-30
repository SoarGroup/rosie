#!/bin/bash

TEST_LANG="python"
FULL_TEST_LIST=(prim-actions test1)

TESTS_TO_RUN=()

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
			echo "By default, it runs all the tests"
			echo "  but you can specify only the tests you want:"
			echo "  run_task_learning_tests test1"
			exit 0
			;;
		-j|--java)
			TEST_LANG="java"
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

cd $ROSIE_HOME/test-agents/task-tests

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
	echo ""
	echo "### Configure Agent"
	java edu.umich.rosie.tools.config.RosieAgentConfigurator $test.config -s

	if [ "$TEST_LANG" == "python" ]; then
		echo ""
		echo "### Running $test using python"
		python3 -m rosie.testing $test
	else
		echo ""
		echo "### Running $test using java"
		java edu.umich.rosie.RosieCLI $test/agent/rosie.$test.config
	fi

	echo ""
	echo "### Comparing test-output vs correct-output for $test: "
	diff output_$test.txt $test/correct-output.txt
	echo ""
	echo "### End of Test $test"
	echo ""
done

