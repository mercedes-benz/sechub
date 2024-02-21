#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

# Purpose for this script:
# * Cleanup after integration tests
# 
# Details:
# - Stops running PDS and SecHub servers (asynchronous)
# 
# Usage: 05-stop.sh $sechubServerPortNr $pdsPortNr
# Example:
# ```
# cd $gitRoot/github-actions/scan
# ./05-stop.sh 8443 8444
# ```
SERVER_PORT=$1

SCRIPT_DIR="$(dirname -- "$0")"
if [ "$SCRIPT_DIR" = "." ]; then
    pwd
    SCRIPT_DIR="$(pwd)"
fi
echo "SCRIPT_DIR = $SCRIPT_DIR"
cd ${SCRIPT_DIR}

./stop_sechub_server.sh $SERVER_PORT
./stop_pds.sh $SERVER_PORT