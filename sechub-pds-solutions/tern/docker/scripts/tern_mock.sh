#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# Mock is scan output of: xyz
echo "Running PDS Tern Mock"
cp "$MOCK_FOLDER/mock.sarif.json" "$PDS_JOB_RESULT_FILE"