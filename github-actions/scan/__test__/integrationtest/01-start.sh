#!/bin/bash
# SPDX-License-Identifier: MIT

set -e

# Purpose for this script:
# * Preparation for integration tests
#
# Details:
# - Calculates pathes
# - Downloads PDS and SecHub server version
# - removes former temp data automatically (e.g. old reports etc.)
# - Starts SecHubServer and PDS
# - Waits for the servers to be alive
# - After this a standard setup is done for SecHub to be able to communicate with PDS, project setup etc.
# - exit of script is done at the end - means synchronous execution
#
# Usage: 01-start.sh $secHubServerVersion $sechubServerPortNr $pdsVersion $pdsPortNr
# Example:
# ```
# cd $gitRoot/github-actions/scan
# ./01-start.sh 1.7.0 8443 1.4.0 8444
# ```
#
SERVER_VERSION=$1
SERVER_PORT=$2

PDS_SERVER_VERSION=$3
PDS_SERVER_PORT=$4

DEBUG=$SECHUB_DEBUG

if [ "$SERVER_VERSION" = "" ]; then
    echo "first argument not set - is used as server version!"
    exit 1
fi
if [ "$SERVER_PORT" = "" ]; then
    echo "second argument not set - is used as server port!"
    exit 1
fi

if [ "$PDS_SERVER_VERSION" = "" ]; then
    echo "third argument not set - is used as PDS server version!"
    exit 1
fi
if [ "$PDS_SERVER_PORT" = "" ]; then
    echo "fourth argument not set - is used as PDS server port!"
    exit 1
fi

## ----------------------------------
##         Prepare variables
## ----------------------------------
echo "> Prepare variables"
SECHUB_BASE_URL="https://localhost:$SERVER_PORT"
PDS_BASE_URL="https://localhost:$PDS_SERVER_PORT"
SCRIPT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )" # absolute directory of this script

if [ "$SCRIPT_DIR" = "." ]; then
    pwd
    SCRIPT_DIR="$(pwd)"
fi
cd "${SCRIPT_DIR}"

cd ../.. #github action scan folder
GHA_SCAN_FOLDER_PATH="$(pwd)"

SANITY_PATH_CHECK_FOR_SCRIPT="$GHA_SCAN_FOLDER_PATH/__test__/integrationtest/01-start.sh"
if [ ! -f "$SANITY_PATH_CHECK_FOR_SCRIPT" ]; then
    echo "Sanity check failed - not found: $SANITY_PATH_CHECK_FOR_SCRIPT"
    exit 1
fi

# Remote:
# GHA_SCANFOLDER_PATH= /home/runner/work/sechub/github-actions/scan
# RUNTIME:             /home/runner/work/sechub/runtime
# WORKSPACE:           /home/runner/work/sechub
WORKSPACE_DIR="${GHA_SCAN_FOLDER_PATH}/../../"
RUNTIME_DIR="${WORKSPACE_DIR}/build/sechub-runtime";
SHARED_VOLUME="${RUNTIME_DIR}/shared-volume"
mkdir -p "$SHARED_VOLUME"

echo "SECHUB_BASE_URL=$SECHUB_BASE_URL"
echo "PDS_BASE_URL   =$PDS_BASE_URL"
echo "SCRIPT_DIR     =$SCRIPT_DIR"
echo "RUNTIME_DIR    =$RUNTIME_DIR"
echo "SHARED_VOLUME  =$RUNTIME_DIR"

## ----------------------------------
##         SecHub Server
## ----------------------------------
echo "> Current working directory :$(pwd)"
echo "> SecHub Server"

SERVER_FOLDER_PATH="${RUNTIME_DIR}/server/${SERVER_VERSION}"
SERVER_EXECUTABLE_NAME="sechub-server-${SERVER_VERSION}.jar"
SERVER_EXECUTABLE_PATH="${SERVER_FOLDER_PATH}/${SERVER_EXECUTABLE_NAME}"
SERVER_DOWNLOAD_URL="https://github.com/mercedes-benz/sechub/releases/download/v${SERVER_VERSION}-server/${SERVER_EXECUTABLE_NAME}"
SERVER_CERTFILE_PATH="${SERVER_FOLDER_PATH}/generated-localhost-certificate.p12"

#echo "SERVER_FOLDER_PATH=$SERVER_FOLDER_PATH"



## download server if not available
if [ ! -f "$SERVER_EXECUTABLE_PATH" ]; then
    mkdir -p "${SERVER_FOLDER_PATH}"
    echo "Start download from $SERVER_DOWNLOAD_URL"
    curl -L "${SERVER_DOWNLOAD_URL}" -o "${SERVER_EXECUTABLE_PATH}"
else 
    echo "$SERVER_EXECUTABLE_NAME exists already - skip download"
fi

## generate SecHub test certificate
if [ ! -f "$SERVER_CERTFILE_PATH" ]; then
    mkdir -p "${SERVER_FOLDER_PATH}"
    keytool -genkey -alias tomcat -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore "${SERVER_CERTFILE_PATH}" -validity 3650 -storepass 123456 --dname "CN=localhost, OU=ID"
fi


# Start SecHub async
SECHUB_LOGFILE="$SERVER_FOLDER_PATH/server.log"

rm -f "$SECHUB_LOGFILE" # remove old log files on start
echo "./start_sechub_server.sh $SERVER_PORT $SERVER_EXECUTABLE_PATH $SERVER_CERTFILE_PATH $SECHUB_LOGFILE $SHARED_VOLUME" > "$SECHUB_LOGFILE"
"$SCRIPT_DIR/start_sechub_server.sh" "$SERVER_PORT" "$SERVER_EXECUTABLE_PATH" "$SERVER_CERTFILE_PATH" "$SECHUB_LOGFILE" "$SHARED_VOLUME"

## ----------------------------------
##             PDS
## ----------------------------------
echo "> PDS"

PDS_SERVER_FOLDER_PATH="${RUNTIME_DIR}/pds/${PDS_SERVER_VERSION}"
PDS_SERVER_EXECUTABLE_NAME="sechub-pds-${PDS_SERVER_VERSION}.jar"
PDS_SERVER_EXECUTABLE_PATH="${PDS_SERVER_FOLDER_PATH}/${PDS_SERVER_EXECUTABLE_NAME}"
PDS_SERVER_DOWNLOAD_URL="https://github.com/mercedes-benz/sechub/releases/download/v${PDS_SERVER_VERSION}-pds/${PDS_SERVER_EXECUTABLE_NAME}"
PDS_SERVER_CERTFILE_PATH="${PDS_SERVER_FOLDER_PATH}/generated-localhost-certificate.p12"

#echo "PDS_SERVER_FOLDER_PATH=$PDS_SERVER_FOLDER_PATH"

## download PDS if not available
if [ ! -f "$PDS_SERVER_EXECUTABLE_PATH" ]; then
    mkdir -p "${PDS_SERVER_FOLDER_PATH}"
    echo "Start download from $PDS_SERVER_DOWNLOAD_URL"
    curl -L "${PDS_SERVER_DOWNLOAD_URL}" -o "${PDS_SERVER_EXECUTABLE_PATH}"
else 
    echo "$PDS_SERVER_EXECUTABLE_NAME exists already - skip download"
fi

## generate PDS test certificate
if [ ! -f "$PDS_SERVER_CERTFILE_PATH" ]; then
    mkdir -p "${PDS_SERVER_FOLDER_PATH}"
    keytool -genkey -alias tomcat -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore "${PDS_SERVER_CERTFILE_PATH}" -validity 3650 -storepass 123456 --dname "CN=localhost, OU=ID"
fi

## create config file for pds
PDS_CONFIG_FILE="$PDS_SERVER_FOLDER_PATH/pds-config.json"
cp "$SCRIPT_DIR/test-config/gha_integrationtest_pds-config.json" "$PDS_CONFIG_FILE"


# Start PDS async
PDS_LOGFILE="$PDS_SERVER_FOLDER_PATH/pds.log"
rm -f "$PDS_LOGFILE" # remove old log files on start

echo "./start_pds.sh $PDS_SERVER_PORT $PDS_SERVER_EXECUTABLE_PATH $PDS_SERVER_CERTFILE_PATH $PDS_LOGFILE $SHARED_VOLUME $PDS_CONFIG_FILE" > "$PDS_LOGFILE"
"$SCRIPT_DIR/start_pds.sh" "$PDS_SERVER_PORT" "$PDS_SERVER_EXECUTABLE_PATH" "$PDS_SERVER_CERTFILE_PATH" "$PDS_LOGFILE" "$SHARED_VOLUME" "$PDS_CONFIG_FILE"

echo "> Wait for SecHub and PDS"
"$SCRIPT_DIR/wait_server_alive.sh" "SecHub Server" "$SECHUB_BASE_URL/api/anonymous/check/alive"
"$SCRIPT_DIR/wait_server_alive.sh" "PDS" "$PDS_BASE_URL/api/anonymous/check/alive"