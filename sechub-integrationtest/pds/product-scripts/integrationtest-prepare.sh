#!/bin/bash
# SPDX-License-Identifier: MIT

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh

dumpPDSVariables

echo "PDS prepare integration test script starting..."

export PDS_PREPARE_EXECUTED=true
dumpVariable "PDS_PREPARE_EXECUTED"

echo "PREPARE does not do anything yet"

## Just add a info message for user in report - we check this in integration test
infoMessage "Some preperation info message for user in report."

if [[ "$PDS_TEST_KEY_VARIANTNAME" == "b" ]]; then
    # Variant b fails at preparation time - just by exit 5...
    errorMessage "Some preperation error message for user in report."
    
    PDS_PREPARE_FAILED=true
    dumpVariable "PDS_PREPARE_FAILED"
    exit 0 # no write of pds result file --> job is marked as failed, but script works
fi

if [[ "$PDS_TEST_KEY_VARIANTNAME" == "c" ]]; then
    errorMessage "This error message will not appear in report - because internal failure"
    exit 5
fi


echo "SECHUB_PREPARE_DONE" > "$PDS_JOB_RESULT_FILE"