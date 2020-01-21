#!/bin/bash

cd $ROSIE_HOME/tools/java
if [ "$1" == "--clean" ]; then
	ant clean
else 
	ant
fi
