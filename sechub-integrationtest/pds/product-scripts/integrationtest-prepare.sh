#!/bin/bash
# SPDX-License-Identifier: MIT

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh

dumpPDSVariables

echo "PDS prepare integration test script starting..."

export PDS_PREPARE_EXECUTED=true
dumpVariable "PDS_PREPARE_EXECUTED"

echo "PREPARE does not do anything yet"

errorMessage "Preparation was not successful!"