#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

SERVER_PORT=$1
PATH_TO_EXECUTABLE=$2
PATH_TO_CERTIFICATE=$3
PATH_TO_LOGFILE=$4
SHARED_VOLUME=$5
PDS_CONFIG_FILE=$6

echo "SERVER_PORT=$SERVER_PORT" >> $PATH_TO_LOGFILE

echo "[ START ] PDS"

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
if [ "$PDS_CONFIG_FILE" = "" ]; then
    echo "sixth argument not set - is used as PDS config file path!"
    exit 1
fi

SCRIPT_DIR="$(dirname -- "$0")"

echo "Start PDS at localhost:${SERVER_PORT}, executable at: ${PATH_TO_EXECUTABLE}"
# `curl -s --insecure https://localhost:${this.serverPort}/api/anonymous/check/alive`;
java \
 -Dfile.encoding=UTF-8 \
 -Dspring.profiles.active=pds_integrationtest,pds_h2 \
 -Dserver.ssl.key-store=${PATH_TO_CERTIFICATE} \
 -Dserver.port=${SERVER_PORT} \
 -Dpds.storage.sharedvolume.upload.dir=$SHARED_VOLUME \
 -Dpds.config.file=$PDS_CONFIG_FILE \
 -jar ${PATH_TO_EXECUTABLE}>>${PATH_TO_LOGFILE} &
