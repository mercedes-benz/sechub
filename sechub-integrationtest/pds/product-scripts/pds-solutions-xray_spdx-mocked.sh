#!/bin/bash 
# SPDX-License-Identifier: MIT

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh
echo "Current directory:"
pwd

echo "PDS solutions XRAY (spdx) mock starting"
cp "./../sechub-pds-solutions/xray/docker/mocks/spdx_mock.json" "$PDS_JOB_RESULT_FILE"

warnMessage "mocked result"
infoMessage "product:xray_spdx"