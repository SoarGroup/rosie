#!/bin/bash

cd $ROSIE_HOME/java
if [ "$1" == "--clean" ]; then
	ant clean
else 
	ant
fi
