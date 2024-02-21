#!/bin/bash
# SPDX-License-Identifier: MIT

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh

dumpPDSVariables

echo "PDS prepare integration test script starting..."

export PDS_PREPARE_EXECUTED=true
dumpVariable "PDS_PREPARE_EXECUTED"

echo "PREPARE does not do anything yet"

## Just add a error message for user in report - we check this in integration test
errorMessage "Some preperation error message for user in report."

echo "{ \"state\" : \"success\"}" > "$PDS_JOB_RESULT_FILE"