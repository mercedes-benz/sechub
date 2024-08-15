#!/bin/bash
# SPDX-License-Identifier: MIT

source "$HELPER_FOLDER/message.sh"

# Mock is scan output of: https://github.com/Contrast-Security-OSS/go-test-bench
infoMessage "Running PDS Mock"
cp "$MOCK_FOLDER/mock.sarif.json" "$PDS_JOB_RESULT_FILE"