#!/bin/bash 
# SPDX-License-Identifier: MIT

# Inside this test script we just do some output which will be fetched from integration tests
# and then inspected and asserted

# So it is just a kind of "echo server" ...
source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh
echo "PDS Web scan starting with variant: '$PDS_TEST_KEY_VARIANTNAME'"

echo "#PDS_INTTEST_PRODUCT_WEBSCAN
info:PDS_SCAN_TARGET_URL=$PDS_SCAN_TARGET_URL,PDS_TEST_KEY_VARIANTNAME=$PDS_TEST_KEY_VARIANTNAME,PRODUCT2_LEVEL=$PRODUCT2_LEVEL
info:PDS_SCAN_CONFIGURATION=$PDS_SCAN_CONFIGURATION
" > "${PDS_JOB_RESULT_FILE}"



dumpPDSVariables

# We added pds.config.templates.metadata.list as optional parameter here for testing
# So we can dump the variable here - used in scenario12 integration test
dumpVariable "PDS_CONFIG_TEMPLATE_METADATA_LIST"

ASSET_FILE1="$PDS_JOB_EXTRACTED_ASSETS_FOLDER/webscan-login/testfile1.txt"
TEST_CONTENT_FROM_ASSETFILE=$(cat $ASSET_FILE1) 
# Afterwards TEST_CONTENT_FROM_ASSETFILE=i am "testfile1.txt" for scenario12 integration tests
dumpVariable "TEST_CONTENT_FROM_ASSETFILE"
    
if [[ "$PDS_TEST_KEY_VARIANTNAME" = "a" ]]; then
     
    infoMessage "info from webscan by PDS for sechub job uuid: $SECHUB_JOB_UUID"
    warnMessage "warning from webscan by PDS for sechub job uuid: $SECHUB_JOB_UUID"
    errorMessage "error from webscan by PDS for sechub job uuid: $SECHUB_JOB_UUID"
    # needed to check if all files where extracted at the PDS side
    infoMessage $(ls $PDS_JOB_EXTRACTED_SOURCES_FOLDER/sechub-integrationtest/src/test/resources/pds-webscan-data-ref-files | tr -d '\n')
fi
