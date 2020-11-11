#!/bin/bash 

if [ $# -lt 1 ]; 
    then echo "Needs 1 argument - the word to grep"
else
	# Only match whole words 
	# (preceeding and following characters are not alphanumeric or a dash/underscore)
	grepcommand="grep --color=always -nR \"\(^\|[^a-zA-Z0-9_-]\)$1\($\|[^a-zA-Z0-9_-]\)\" ./"
	eval $grepcommand
fi

