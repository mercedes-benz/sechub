#!/bin/sh
# SPDX-License-Identifier: MIT

. "$SCRIPT_FOLDER/common.sh"

# Mock is scan output of: https://github.com/lucideus-repo/UnSAFE_Bank
echo "Running Gitleaks Mock"
cp "$MOCK_FOLDER/mock.sarif.json" "$PDS_JOB_RESULT_FILE"
