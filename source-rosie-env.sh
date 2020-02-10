# Requires $ROSIE_HOME being set previously to this directory

### Environment Variables
export ROSIE_AGENT=$ROSIE_HOME/agent
export CLASSPATH=$CLASSPATH:$ROSIE_HOME/java/rosie.jar
export CLASSPATH=$CLASSPATH:$ROSIE_HOME/tools/java/rosie-tools.jar
export CLASSPATH=$CLASSPATH:$ROSIE_HOME/tools/antlr-4.5-complete.jar

export PYTHONPATH=$PYTHONPATH:$ROSIE_HOME/tools/python


### Rosie Scripts

# build_rosie: Compiles the rosie java project
alias build_rosie="$ROSIE_HOME/tools/scripts/build_rosie.sh"

# build_tools: Compiles the rosie java tools
alias build_tools="$ROSIE_HOME/tools/scripts/build_rosie_tools.sh"

# Custom Command Line Completions
source $ROSIE_HOME/tools/scripts/custom_completions.sh

# build_agent <agent>: Runs the RosieAgentConfigurator tool on the given test-agent
alias build_agent="$ROSIE_HOME/tools/scripts/build_agent.sh"
complete -F _rosie_agents_completion build_agent

# run_rosie <agent>: Runs rosie using the BasicRosieGUI using the given test-agent's config file
#   Add -c to run in CLI mode
#   Add -m to run with the mobile simulator GUI
#   Add -b to run in the background
alias run_rosie="$ROSIE_HOME/tools/scripts/run_rosie.sh"
complete -F _rosie_agents_completion run_rosie

# make_rosie_world <map_info>: Will take the given map info file
#   and build map and world files for both rosie and the mobile simulator
alias make_rosie_world="$ROSIE_HOME/tools/scripts/make_rosie_world.sh"
complete -F _map_info_completion make_rosie_world

# run_task_learning_tests: Will run task learning tests under test-agents/task-tests
#   and print out discrepancies
#   Add -j to use the java testing tool instead of the python default
alias run_task_learning_tests="$ROSIE_HOME/tools/scripts/run_task_learning_tests.sh"

### Other Useful Scripts

# soar-find-replace <word1> <word2>: Will replace word1 with word2 in all subfiles, 
#   but consider entire soar words only 
#   (will not match parts of rules/attributes/variables)
alias soar-find-replace="$ROSIE_HOME/tools/scripts/find_replace_soar.sh"

# grep-agent <pattern>: Will grep the agent folder for the given pattern, ignoring language-comprehension, game-learning, and testing files
alias grep-agent="$ROSIE_HOME/tools/scripts/grep_agent.sh"
