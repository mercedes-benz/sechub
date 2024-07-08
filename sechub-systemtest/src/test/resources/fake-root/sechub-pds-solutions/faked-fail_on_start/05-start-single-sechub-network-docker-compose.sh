#!/usr/bin/bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")
echo "[START] faked-fail_on_start:05-start-single-sechub-network-docker-compose.sh"

echo "This shall be the last output message"
echo "This shall be the last fail message" >&2

exit 33 # keep the strange exitcode - we use this inside test
