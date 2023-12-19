#!/bin/bash 
# SPDX-License-Identifier: MIT

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh
echo "Current directory:"
pwd

echo "PDS solutions GITLEAKS mock starting"
cp "./../sechub-pds-solutions/gitleaks/docker/mocks/mock.sarif.json" "$PDS_JOB_RESULT_FILE"

warnMessage "mocked result"
infoMessage "product:gitleaks"