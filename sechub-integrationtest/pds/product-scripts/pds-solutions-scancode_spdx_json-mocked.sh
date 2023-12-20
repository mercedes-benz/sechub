#!/bin/bash 
# SPDX-License-Identifier: MIT

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh
echo "Current directory:"
pwd

echo "PDS solutions SCANCODE mock starting"
cp "./../sechub-pds-solutions/scancode/docker/mocks/scancode_mock.spdx.json" "$PDS_JOB_RESULT_FILE"

warnMessage "mocked result"
infoMessage "product:scancode"