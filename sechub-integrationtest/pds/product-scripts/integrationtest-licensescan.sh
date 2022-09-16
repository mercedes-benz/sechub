#!/bin/bash 
# SPDX-License-Identifier: MIT

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh

echo "PDS License integrationt test script starting..."

dumpPDSVariables

cp "$PDS_JOB_EXTRACTED_SOURCES_FOLDER/sample_spdx.json" $PDS_JOB_RESULT_FILE