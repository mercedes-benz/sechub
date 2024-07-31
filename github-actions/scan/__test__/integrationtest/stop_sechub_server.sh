#!/usr/bin/bash
# SPDX-License-Identifier: MIT

# $1 = server port
SERVER_PORT=$1
echo "[ STOP  ] SecHub"
echo "Shutdown SecHub server at localhost:${SERVER_PORT}"
if [ "$SERVER_PORT" = "" ]; then
    echo "first argument not set - is used as server port!"
    exit 1
fi
curl -s --insecure "https://localhost:${SERVER_PORT}/api/anonymous/integrationtest/shutdown"
echo "Shutdown initiated"
