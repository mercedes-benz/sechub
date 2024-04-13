#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# Mock is scan output of: https://github.com/lucideus-repo/UnSAFE_Bank
echo "Running PDS mobsfscan Mock"
cp "$MOCK_FOLDER/mobsfscan_mock.sarif.json" "$PDS_JOB_RESULT_FILE"
