#!/bin/bash
# SPDX-License-Identifier: MIT

set -e

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
PDS_PORT=$2

SCRIPT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )" # absolute directory of this script
echo "SCRIPT_DIR = $SCRIPT_DIR"
cd "${SCRIPT_DIR}"

./stop_sechub_server.sh "$SERVER_PORT"
./stop_pds.sh "$PDS_PORT"