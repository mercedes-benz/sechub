#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# Mock is scan output of: https://github.com/OWASP/NodeGoat
echo "Running PDS njsscan Mock"
cp "$MOCK_FOLDER/njsscan_mock.sarif.json" "$PDS_JOB_RESULT_FILE"
