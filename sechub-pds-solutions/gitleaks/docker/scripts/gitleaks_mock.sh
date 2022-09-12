#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# Mock is scan output of: https://github.com/lucideus-repo/UnSAFE_Bank
echo "Running Gitleaks Mock"
cp "$MOCK_FOLDER/mock.sarif.json" "$PDS_JOB_RESULT_FILE"