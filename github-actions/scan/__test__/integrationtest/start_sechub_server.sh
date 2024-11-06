#!/bin/bash
# SPDX-License-Identifier: MIT

SERVER_PORT=$1
PATH_TO_EXECUTABLE=$2
PATH_TO_CERTIFICATE=$3
PATH_TO_LOGFILE=$4
SHARED_VOLUME=$5
echo "SERVER_PORT=$SERVER_PORT" >> "$PATH_TO_LOGFILE"

echo "[ START ] SecHub"

if [ "$SERVER_PORT" = "" ]; then
    echo "first argument not set - is used as server port!"
    exit 1
fi
if [ "$PATH_TO_EXECUTABLE" = "" ]; then
    echo "second argument not set - is used as server jar executable path!"
    exit 1
fi

if [ "$PATH_TO_CERTIFICATE" = "" ]; then
    echo "third argument not set - is used as server certificate path!"
    exit 1
fi
if [ "$PATH_TO_LOGFILE" = "" ]; then
    echo "fourth argument not set - is used as server log file path!"
    exit 1
fi
if [ "$SHARED_VOLUME" = "" ]; then
    echo "fifth argument not set - is used as shared volume path!"
    exit 1
fi

SCRIPT_DIR="$(dirname -- "$0")"

export SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR=$SHARED_VOLUME

echo "Start SecHub server at localhost:${SERVER_PORT}, executable at: ${PATH_TO_EXECUTABLE}"
# `curl -s --insecure https://localhost:${this.serverPort}/api/anonymous/check/alive`;
java \
 -Dfile.encoding=UTF-8 \
 -Dspring.profiles.active=dev,mocked_products,h2,integrationtest \
 -Dserver.ssl.key-store="${PATH_TO_CERTIFICATE}" \
 -Dsechub.server.debug=true \
 -Dserver.port="${SERVER_PORT}" \
 -Dsechub.integrationtest.ignore.missing.serverproject=true \
 -jar "${PATH_TO_EXECUTABLE}">>"${PATH_TO_LOGFILE}" &
