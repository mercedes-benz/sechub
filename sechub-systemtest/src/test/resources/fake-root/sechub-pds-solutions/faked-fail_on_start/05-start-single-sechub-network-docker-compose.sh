#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")
echo "[START] faked-fail_on_start:05-start-single-sechub-network-docker-compose.sh"

echo "This shall be the last output message"
echo "This shall be the last fail message" >&2

exit 1 # keep this at line 10, we check this inside our tests
