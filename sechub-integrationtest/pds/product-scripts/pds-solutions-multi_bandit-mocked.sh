#!/bin/bash 
# SPDX-License-Identifier: MIT

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh
echo "Current directory:"
pwd

echo "PDS solutions MULTI_BANDIT mock starting"
cp "./../sechub-pds-solutions/multi/docker/mocks/bandit_mock.sarif.json" "$PDS_JOB_RESULT_FILE"

warnMessage "mocked result"
infoMessage "product:multi_bandit"