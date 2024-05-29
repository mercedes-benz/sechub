#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

SERVER_TYPE=$1
CHECK_ALIVE_URL=$2

if [ "$CHECK_ALIVE_URL" = "" ]; then
    echo "first argument not set - is used as url to check alive via head!"
    exit 1
fi

echo "[ WAIT ] Until $SERVER_TYPE alive at $CHECK_ALIVE_URL"

declare -i MAX_WAIT_SECONDS=60
declare -i waitCount=0

## Wait until available
printf '.'
# shellcheck disable=SC2091
until $(curl --output /dev/null --silent --head --fail --insecure "$CHECK_ALIVE_URL"); do
    printf '.'
    if [ $waitCount -gt $MAX_WAIT_SECONDS ]; then
        echo ""
        echo "[ERROR] WAIT max time exceeded: $MAX_WAIT_SECONDS seconds"
        exit 1
    fi
    sleep 1
    waitCount=$waitCount+1
done

echo ""
echo "> $SERVER_TYPE is alive"
