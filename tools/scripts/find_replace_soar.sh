#!/bin/bash 

if [ $# -lt 2 ]; 
    then echo "Needs 2 arguments"
fi

# find all soar files
findcommand="find ./ -type f -name \"*.soar\" -print0"

## Replace variable tags - '<example>'
findreplace="s/<"$1">/<"$2">/g"
echo $findreplace
eval $findcommand | xargs -0 sed -i "$findreplace"

# Replace words in a rule
#   previous character is { or *
#   following character is * or EOL
findreplace="s/\([{*]\)"$1"\(\*\|\$\)/\1"$2"\2/g"
echo $findreplace
eval $findcommand | xargs -0 sed -i "$findreplace"

# Replace words in an attribute if:
#   previous character is ^ or .
#   following character is . or ) or space or EOL
findreplace="s/\([.^]\)"$1"\([.) ]\|\$\)/\1"$2"\2/g"
echo $findreplace
eval $findcommand | xargs -0 sed -i "$findreplace"

# Replace values:
#   previous character is space
#   following character is space or ) or EOL
findreplace="s/\([ ]\)"$1"\([ )]\|\$\)/\1"$2"\2/g"
echo $findreplace
eval $findcommand | xargs -0 sed -i "$findreplace"

# Replace words surrounded by whitespace
#   previous character is space or BOL
#   following character is space or EOL
findreplace="s/\(^\|[ ]\)"$1"\([ ]\|\$\)/\1"$2"\2/g"
echo $findreplace
eval $findcommand | xargs -0 sed -i "$findreplace"

