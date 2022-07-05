#!/bin/bash 
# SPDX-License-Identifier: MIT

echo "PDS License integrationt test script starting..."
echo ">PDS_JOB_HAS_EXTRACTED_SOURCES =$PDS_JOB_HAS_EXTRACTED_SOURCES"
echo ">PDS_JOB_HAS_EXTRACTED_BINARIES=$PDS_JOB_HAS_EXTRACTED_BINARIES"

cp "$PDS_JOB_EXTRACTED_SOURCES_FOLDER/sample_spdx.json" $PDS_JOB_RESULT_FILE