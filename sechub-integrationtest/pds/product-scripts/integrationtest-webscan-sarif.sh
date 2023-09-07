#!/bin/bash 
# SPDX-License-Identifier: MIT

function errEcho () {
    echo "$@" >&2
}

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh

echo "PDS webscan SARIF integrationt test script starting..."

dumpPDSVariables

echo "PDS SARIF web scan test script starting... "
echo "- configured variant:'$PDS_TEST_KEY_VARIANTNAME'"
echo "- current working directory: $(pwd)"

if [[ "$PDS_TEST_KEY_VARIANTNAME" = "b" ]]; then
    # variant b is used in test executor profile 8
    cp ./../sechub-integrationtest/src/test/resources/pds/webscan/webscan-result-variant-b.sarif.json "$PDS_JOB_RESULT_FILE"
else
  	errEcho "Unsupported variant: '$PDS_TEST_KEY_VARIANTNAME'. Must be implemented! Will exit now with failure."
  	exit 1
fi
