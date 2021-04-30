#!/bin/bash

## Autocomplete map files in $ROSIE_HOME/tools/map_info
#_map_info_completion()
#{
#	local cur=${COMP_WORDS[COMP_CWORD]}
#	COMPREPLY=( $(compgen -W "$(ls $ROSIE_HOME/tools/map_info)" -- $cur) )
#}

# Autocomplete rosie agents in $ROSIE_HOME/test-agents
_rosie_agents_completion()
{
	local cur=${COMP_WORDS[COMP_CWORD]}
	COMPREPLY=( $(compgen -W "$(ls $ROSIE_HOME/test-agents)" -- $cur) )
}

# Autocomplete task-tests in $ROSIE_HOME/test-agents/task-tests
_task_tests_completion()
{
	local cur=${COMP_WORDS[COMP_CWORD]}
	# Find all directories in task-tests 
	COMPREPLY=( $(compgen -W "$(find $ROSIE_HOME/test-agents/task-tests/* -maxdepth 0 -type d -printf '%f ')" -- $cur) )
}

# Autocomplete examples in $ROSIE_HOME/examples
_rosie_examples_completion()
{
	local cur=${COMP_WORDS[COMP_CWORD]}
	# Find all directories in rosie/examples
	COMPREPLY=( $(compgen -W "$(find $ROSIE_HOME/examples/* -maxdepth 0 -type d -printf '%f ')" -- $cur) )
}


