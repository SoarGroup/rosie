#!/bin/bash

containsElement () {
	if [ $# -lt 1 ]; then
		echo "containsElement: requires 2 arguments - 1:element 2+:array items"
		return 1
	fi

	local element=$1; shift
	for e; do
		if [[ $e == $element ]]; then
			return 0
		fi
	done
	return 1
}


_probcog_worlds_completion()
{
	local cur=${COMP_WORDS[COMP_CWORD]}

	if $(containsElement "-a" "${COMP_WORDS[@]}") || 
		$(containsElement "--arm" "${COMP_WORDS[@]}"); then
		export PROBCOG_HOME=$ROSIE_PROJ/arm
	elif $(containsElement "-f" "${COMP_WORDS[@]}") || 
		$(containsElement "--fetch" "${COMP_WORDS[@]}"); then
		export PROBCOG_HOME=$ROSIE_PROJ/fetch
	else
		export PROBCOG_HOME=$ROSIE_PROJ/probcog
	fi

	COMPREPLY=( $(compgen -W "$(ls $PROBCOG_HOME/worlds)" -- $cur) )
}


