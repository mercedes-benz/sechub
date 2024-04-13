#!/bin/bash 
# SPDX-License-Identifier: MIT

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh

#echo "PDS Secret Scan integration test script starting..."
ls "$PDS_JOB_EXTRACTED_SOURCES_FOLDER/"

dumpPDSVariables

cp "$PDS_JOB_EXTRACTED_SOURCES_FOLDER/gitleaks_sample_sarif.json" "$PDS_JOB_RESULT_FILE"