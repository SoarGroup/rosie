#!/bin/bash

_rosie_agents_completion()
{
	local cur=${COMP_WORDS[COMP_CWORD]}
	COMPREPLY=( $(compgen -W "$(ls $ROSIE_HOME/test-agents)" -- $cur) )
}


