#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")

echo "[START] faked-gosec:05-start-single-sechub-network-docker-compose.sh - A_TEST1=$A_TEST1"

echo "gosec-started with param2=$2 and C_test_var_number_added=$C_test_var_number_added, B_TEST2=$B_TEST2" > $1 # Write for test 