#!/usr/bin/bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")

echo "[STOP ] faked-sechub:01-stop-single-docker-compose.sh $1"
echo "sechub-stopped with param2=$2 and parm3=$3 and Y_TEST=$Y_TEST" > "$1" # Write for test