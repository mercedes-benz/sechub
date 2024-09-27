#!/bin/bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")

echo "[START] faked-sechub:01-start-single-docker-compose.sh $1"

if [[ "$1" != "" ]]; then
   echo "sechub-started and TEST_NUMBER_LIST=$TEST_NUMBER_LIST" > "$1" # write this as output to file path inside $1
else
   echo "sechub-started - no output"
fi

