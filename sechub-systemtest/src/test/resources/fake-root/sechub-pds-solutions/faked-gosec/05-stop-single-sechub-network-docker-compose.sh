#!/bin/bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")

echo "[STOP ] faked-gosec:05-stop-single-sechub-network-docker-compose.sh"

echo "gosec-stopped with param2=$2 and parm3=$3 and X_TEST=$X_TEST" > "$1" # Write for test 