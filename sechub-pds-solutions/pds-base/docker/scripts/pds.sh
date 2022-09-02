#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# Mock is scan output of: https://github.com/Contrast-Security-OSS/go-test-bench
echo "Running PDS Mock"
cp "$MOCK_FOLDER/mock.sarif.json" "$PDS_JOB_RESULT_FILE"