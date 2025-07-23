#!/bin/bash 
# SPDX-License-Identifier: MIT

set -eE  # same as: `set -o errexit -o errtrace`
trap 'echo "ERROR: $BASH_SOURCE:$LINENO -> '"'"'$BASH_COMMAND'"'"' returned exit code: $?" >&2' ERR

echo "################################################"
echo "# Start PDS infralight integration test script #"
echo "################################################"

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh

export PDS_INTEGRATIONTEST_ENABLED=true
export TOOL_FOLDER=./../sechub-integrationtest/build/pds-tools # gradle bootJar task does inject the
                                                               # wrapper jar here for testing
dumpPDSVariables
# dump additional variables intersting for integration tests:

echo ""
echo "- start sourcing the pds-solution script"
echo ""

# next line uses same start script as the original - which will start the wrapper!
source ./../sechub-pds-solutions/infralight/docker/scripts/infralight.sh

