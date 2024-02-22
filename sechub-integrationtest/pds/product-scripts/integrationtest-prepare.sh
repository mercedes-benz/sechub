#!/bin/bash
# SPDX-License-Identifier: MIT

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh

echo "1. Prepare script starting"

dumpPDSVariables

echo "PDS prepare integration test script starting..."

export PDS_PREPARE_EXECUTED=true
dumpVariable "PDS_PREPARE_EXECUTED"

echo "2. Trigger now always an info message"

## Just add a info message for user in report - we check this in integration test
infoMessage "Some preperation info message for user in report (always)."



echo "3. Variant parts starting"
if [[ "$PDS_TEST_KEY_VARIANTNAME" == "b" ]]; then
    # Variant b - fail as expected: write result, with status "failed"
    errorMessage "Some preperation error message for user in report."
    
    PDS_PREPARE_FAILED_NO_RESULT_FILE_BUT_EXIT_0=true
    dumpVariable "PDS_PREPARE_FAILED_NO_RESULT_FILE_BUT_EXIT_0"
    
    echo "3.1 Prepare failure, will write result, with state:failed"
    echo "SECHUB_PREPARE_RESULT;status=failed" > "$PDS_JOB_RESULT_FILE"
    exit 0 
fi

if [[ "$PDS_TEST_KEY_VARIANTNAME" == "c" ]]; then
    # Variant c fails with exit 5 -> simulate script error
    PDS_PREPARE_FAILED_NO_RESULT_FILE_AND_EXIT_5=true
    dumpVariable "PDS_PREPARE_FAILED_NO_RESULT_FILE_AND_EXIT_5"
    
    # next error message appears not in report - for multiple reasons
    # 1. no result file -> no import -> no user messages imported...
    # 2. even when result file: exit code !=0 -> result should not be imported to DB
    errorMessage "This error message will not appear in report."
    echo "3.2 Will now do exit 5"
    exit 5
fi

# Otherwise - prepare done, write result with status "ok"
echo "3.3. Prepare done, write result with state done"
echo "SECHUB_PREPARE_RESULT;status=ok" > "$PDS_JOB_RESULT_FILE"

