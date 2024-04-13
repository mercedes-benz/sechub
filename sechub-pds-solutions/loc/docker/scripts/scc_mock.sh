#!/bin/bash
# SPDX-License-Identifier: MIT

echo "Running PDS Scc Mock"

# Mock is scan output of: /pds of this container
cp "$MOCK_FOLDER/scc-mock.json" "$PDS_JOB_RESULT_FILE"
