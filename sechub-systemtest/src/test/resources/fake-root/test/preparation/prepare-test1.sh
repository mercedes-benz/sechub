#!/bin/bash 

# first argument is the target location for output
workspaceOutputFile=$1

echo "Output from prepare-test1.sh" > $workspaceOutputFile

sleep 10s

echo "prepare-test1.sh [DONE]"