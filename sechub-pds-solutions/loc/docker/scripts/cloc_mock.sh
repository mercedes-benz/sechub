#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# Mock is scan output of: https://github.com/Contrast-Security-OSS/go-test-bench
echo "Running PDS cloc Mock"
cp "$MOCK_FOLDER/cloc-mock.json" "$PDS_JOB_RESULT_FILE"
