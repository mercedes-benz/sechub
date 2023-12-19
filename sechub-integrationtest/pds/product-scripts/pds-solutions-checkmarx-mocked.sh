#!/bin/bash 
# SPDX-License-Identifier: MIT

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh
echo "Current directory:"
pwd

echo "PDS solutions CHECKMARX mock starting"
cp "./../sechub-pds-solutions/checkmarx/docker/mocks/checkmarx-mockdata-multiple.xml" "$PDS_JOB_RESULT_FILE"

warnMessage "mocked result"
infoMessage "product:checkmarx"