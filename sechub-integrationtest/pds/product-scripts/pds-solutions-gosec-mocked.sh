#!/bin/bash 
# SPDX-License-Identifier: MIT

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh
echo "Current directory:"
pwd

echo "PDS solutions GOSEC mock starting"
cp "./../sechub-pds-solutions/gosec/docker/mocks/mock.sarif.json" "$PDS_JOB_RESULT_FILE"

warnMessage "mocked result"
infoMessage "product:gosec"