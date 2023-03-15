#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# Mock is scan output of: sechub-test-alpine
echo "Running PDS Tern Mock"
cp "$MOCK_FOLDER/sechub-test-alpine.spdx.json" "$PDS_JOB_RESULT_FILE"