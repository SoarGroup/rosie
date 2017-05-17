#!/bin/bash
file="noones"
while IFS= read line
do
	echo $line
	grep -nR "$line[0-9]" --exclude=noones ./
done <"$file"
