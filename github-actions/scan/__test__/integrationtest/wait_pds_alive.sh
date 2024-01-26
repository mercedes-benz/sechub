#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

SERVER_PORT=$1

echo "[ WAIT ] Until PDS alive"

if [ "$SERVER_PORT" = "" ]; then
    echo "first argument not set - is used as server port!"
    exit 1
fi

declare -i MAX_WAIT_SECONDS=30
declare -i waitCount=0

## Wait until available
until $(curl --output /dev/null --silent --head --fail --insecure https://localhost:${SERVER_PORT}/api/anonymous/check/alive); do
    printf '.'
    if [ $waitCount -gt $MAX_WAIT_SECONDS ]; then
        echo "WAIT max time exceeded: $MAX_WAIT_SECONDS seconds"
        exit 1
    fi
    sleep 1
    waitCount=$waitCount+1
done

echo ""
echo "> PDS is alive"
