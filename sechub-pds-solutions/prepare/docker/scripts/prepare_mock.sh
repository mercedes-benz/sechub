#!/bin/bash
# SPDX-License-Identifier: MIT

echo ""
echo "---------"
echo "PDS Setup"
echo "---------"
echo ""
echo "SecHub Job UUID: $SECHUB_JOB_UUID"
echo "PDS Job UUID: $PDS_JOB_UUID"
echo ""

echo "Running PDS Prepare Mock"
cp "$MOCK_FOLDER/prepare-mock-status-ok.txt" "$PDS_JOB_RESULT_FILE"