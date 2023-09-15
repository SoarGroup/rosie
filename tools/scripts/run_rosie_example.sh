#!/bin/bash
# Usage: run_rosie_example [example-name]

cd $ROSIE_HOME/examples/$1
make run_example $2 $3
