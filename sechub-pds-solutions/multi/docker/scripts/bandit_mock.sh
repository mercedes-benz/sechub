#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# Mock is scan output of: https://github.com/adeyosemanputra/pygoat
echo "Running PDS Bandit Mock"
cp "$MOCK_FOLDER/bandit_mock.sarif.json" "$PDS_JOB_RESULT_FILE"
