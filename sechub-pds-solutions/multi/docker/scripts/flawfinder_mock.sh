#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# Mock is scan output of: https://github.com/pwk4m1/Damn_Vulnerable_Device_Driver
echo "Running PDS Flawfinder Mock"
cp "$MOCK_FOLDER/flawfinder_mock.sarif.json" "$PDS_JOB_RESULT_FILE"
