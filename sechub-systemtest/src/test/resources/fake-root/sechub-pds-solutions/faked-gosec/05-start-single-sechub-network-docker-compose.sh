#!/usr/bin/bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")

echo "[START] faked-gosec:05-start-single-sechub-network-docker-compose.sh - A_TEST1=$A_TEST1"
echo "D_RESOLVED_SECRET=$D_RESOLVED_SECRET"
echo "PATH             =$PATH"
if [[ "$PATH" = "$D_RESOLVED_SECRET" ]]; then
    LIKE_PATH="true"
else
    LIKE_PATH="false"
fi
echo "PARAM3            =$3"
if [[ "$3" = 'third-as:${secretEnv.PATH}_may_not_be_resolved_because_only_script_env_can_contain_this' ]]; then
    PARAM3_STILL_A_SECRET="true"
else
    PARAM3_STILL_A_SECRET="false"
fi

# now we just wait some milliseconds without writing to disk
# - we need this to test stage wait is correct implemented
sleep 0.3s

echo "gosec-started with param2=$2 and C_test_var_number_added=$C_test_var_number_added, B_TEST2=$B_TEST2, D_RESOLVED_SECRET is like path=$LIKE_PATH, parameter3 is still a secret=${PARAM3_STILL_A_SECRET}" > "$1" # Write for test
## Wait again
sleep 0.3s 
