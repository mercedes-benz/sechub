#!/bin/bash 
# SPDX-License-Identifier: MIT

set -e
echo "###############################################"
echo "# Start PDS checkmarx integration test script #"
echo "###############################################"

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh

export PDS_DEBUG_ENABLED=true
export PDS_CHECKMARX_MOCKING_ENABLED=true
export TOOL_FOLDER=./../sechub-integrationtest/build/pds-tools # gradle bootJar task does inject the
                                                               # wrapper jar here for testing
dumpPDSVariables
# dump additional variables intersting for integration tests:
dumpVariable "PDS_CHECKMARX_MOCKING_ENABLED"
dumpVariable "PDS_CHECKMARX_BASEURL"
dumpVariable "PDS_CHECKMARX_ENGINE_CONFIGURATION_NAME"

echo ""
echo "- start sourcing the pds-solution script"
echo ""
# next line uses same checkmarx start script as the original - will start the wrapper!
source ./../sechub-pds-solutions/checkmarx/docker/checkmarx.sh

