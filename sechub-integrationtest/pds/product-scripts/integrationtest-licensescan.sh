#!/bin/bash 
# SPDX-License-Identifier: MIT

echo "PDS License integrationt test script starting..."

#TARGET="$PDS_JOB_WORKSPACE_LOCATION/output/result.txt"
cp "$PDS_JOB_WORKSPACE_LOCATION/upload/unzipped/sourcecode/sample_spdx.json" $PDS_JOB_RESULT_FILE