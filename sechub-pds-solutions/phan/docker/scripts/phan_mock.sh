#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# Mock is scan output of: https://phan.github.io/demo/
echo "Running PDS Phan Mock"
cp "$MOCK_FOLDER/mock.txt" "$PDS_JOB_RESULT_FILE"
