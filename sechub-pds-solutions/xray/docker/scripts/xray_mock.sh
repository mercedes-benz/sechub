#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# returns scan result of Xray for digininja/dvwa version 9b787fc
# mock can return SPDX or CycloneDX report

mock_file="spdx_mock.json"
# mock_file="cycloneDX_mock.json"

echo "Running PDS Xray Mock"
cp "$MOCK_FOLDER/$mock_file" "$PDS_JOB_RESULT_FILE"

