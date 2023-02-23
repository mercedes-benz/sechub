#!/bin/bash

# Get sechub's root folder
currentDir=$(pwd)
baseDir="$currentDir"
regex=sechub$

while ! [[ "$baseDir" =~ ${regex} ]]
do
baseDir="$(dirname $baseDir)"
done


# Copy all scripts from the current folder
cp ./* "$baseDir"
rm "$baseDir/run-all-steps.sh"
cd $baseDir

sechub_pid=0
pds_pid=0

stopServers(){
    kill "$sechub_pid"
    kill "$pds_pid"
    exit 0
}

# Start SecHub
$(sh ./start-sechub.sh) &
sechub_pid=$!

sleep 15

# Start PDS+GoSec
$(sh ./start-pds-gosec.sh) &
pds_pid=$!

sleep 15

# Configure project and variables
# sh ./setup-project.sh

trap 'stopServers' INT

echo "Press Ctrl+C to stop all servers"

# Wait for the user to press Ctrl+C
while true; do
    sleep 1
done