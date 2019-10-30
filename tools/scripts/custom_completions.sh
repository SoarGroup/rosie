#!/bin/bash

# Autocomplete map files in $ROSIE_HOME/tools/map_info
_map_info_completion()
{
	local cur=${COMP_WORDS[COMP_CWORD]}
	COMPREPLY=( $(compgen -W "$(ls $ROSIE_HOME/tools/map_info)" -- $cur) )
}

# Autocomplete rosie agents in $ROSIE_HOME/test-agents
_rosie_agents_completion()
{
	local cur=${COMP_WORDS[COMP_CWORD]}
	COMPREPLY=( $(compgen -W "$(ls $ROSIE_HOME/test-agents)" -- $cur) )
}

