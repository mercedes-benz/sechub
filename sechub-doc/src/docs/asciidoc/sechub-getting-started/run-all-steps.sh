#!/bin/bash
# SPDX-License-Identifier: MIT

SECHUB_SERVER="https://localhost:8443"

# Get sechub's root folder
currentDir=$(pwd)
baseDir="$currentDir"
regex=sechub$

while ! [[ "$baseDir" =~ ${regex} ]]; do
    baseDir="$(dirname "$baseDir")"
done

# Copy all scripts from the current folder
cp ./* "$baseDir"
rm "$baseDir/run-all-steps.sh"
cd "$baseDir" || exit

stopServers() {
    sh ./stop-sechub.sh "no" "./sechub-solution/docker-compose_sechub"
    sh ./stop-pds-gosec.sh "./sechub-pds-solutions/gosec/docker-compose_pds_gosec_external-network.yaml"
    exit 0
}

# Start SecHub
nohup "$(sh ./start-sechub.sh)" &

while true; do
    echo "Waiting for SecHub server to start.."
    isStarted="$(curl -sw '%{http_code}' -k $SECHUB_SERVER/api/anonymous/check/alive)"
    if [ "$isStarted" == "200" ]; then
        break
    fi
    sleep 5
done
echo "SecHub Server started successfully!"

# Start PDS+GoSec
nohup "$(sh ./start-pds-gosec.sh)" &

trap 'stopServers' INT

echo "Press Ctrl+C to stop all servers"

# Wait for the user to press Ctrl+C
while true; do
    sleep 1
done