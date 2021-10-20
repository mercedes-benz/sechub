#!/bin/bash 

# Inside this test script we just do some output which will be fetched from integration tests
# and then inspected and asserted

echo "#PDS_INTTEST_PRODUCT_WEBSCAN
info:PDS_SCAN_TARGET_URL=$PDS_SCAN_TARGET_URL,PDS_TEST_KEY_VARIANTNAME=$PDS_TEST_KEY_VARIANTNAME,PRODUCT2_LEVEL=$PRODUCT2_LEVEL
info:PDS_SCAN_CONFIGURATION=$PDS_SCAN_CONFIGURATION
" > ${PDS_JOB_RESULT_FILE}
