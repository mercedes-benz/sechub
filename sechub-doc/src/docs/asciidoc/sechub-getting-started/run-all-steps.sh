#!/bin/bash
# SPDX-License-Identifier: MIT

SECHUB_SERVER="https://localhost:8443"

# Get sechub's root folder
baseDir="$(git rev-parse --show-toplevel)" 

# Copy all scripts from the current folder
cp ./* "$baseDir"
rm "$baseDir/run-all-steps.sh"
cd "$baseDir" || exit

stopServers() {
    sh ./stop-sechub.sh
    sh ./stop-pds-gosec.sh
    exit $1
}

trap 'stopServers 0' INT

# Start SecHub
nohup sh ./start-sechub.sh &

# Wait until sechub is started
TRIES=0
MAX_TRIES=10
while [ $TRIES -le $MAX_TRIES ]; do
    
    echo "Waiting for SecHub server to start.."
    isStarted="$(curl -sw '%{http_code}' -k $SECHUB_SERVER/api/anonymous/check/alive)"
    if [ "$isStarted" == "200" ]; then
        break
    fi

    TRIES=$((TRIES + 1))
    sleep 5
done

if [ "$TRIES" -gt 10 ]; then
    echo "Couldn't start SecHub Server!"
    stopServers 1
fi

echo "SecHub Server started successfully!"

# Start PDS+GoSec
echo "Starting the PDS server..."
nohup sh ./start-pds-gosec.sh &

sleep 15
# Configure project
echo "Configuring Project..."
sh ./setup-project.sh

echo "Press Ctrl+C to stop all servers"

# Wait for the user to press Ctrl+C
while true; do
    sleep 1
done
