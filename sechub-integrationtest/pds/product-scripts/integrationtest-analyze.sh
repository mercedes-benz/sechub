#!/bin/bash 
# SPDX-License-Identifier: MIT

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh

echo "PDS Analytics integrationt test script starting..."

dumpPDSVariables

cp "./../sechub-integrationtest/pds/data/cloc-json-1.json" $PDS_JOB_RESULT_FILE

