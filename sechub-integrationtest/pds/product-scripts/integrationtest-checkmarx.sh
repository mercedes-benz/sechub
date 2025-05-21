#!/bin/bash 
# SPDX-License-Identifier: MIT

set -eE  # same as: `set -o errexit -o errtrace`
trap 'echo "ERROR: $BASH_SOURCE:$LINENO -> '"'"'$BASH_COMMAND'"'"' returned exit code: $?" >&2' ERR

echo "###############################################"
echo "# Start PDS checkmarx integration test script #"
echo "###############################################"

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh

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
source ./../sechub-pds-solutions/checkmarx/docker/scripts/checkmarx.sh

# -----------------------
# Postcheck of zip upload
# -----------------------
# At the end let us check the "recompressed" folder. Reason: We use a mocked adapter here.
# The mocked adapters do only check the folder paths inside the adapter configuration and just
# return a mock result depending on the found path.
#   
# "Normal" PDS integration tests do not use a mocked adapter, but here we have a real PDS adapter
# in use which calls a script (this one) which uses a wrapper, which uses a mocked adapter (checkmarx)
# to simulate server calling...
#
# To check the re-compression works as expected, we can inspect the recompressed parts here after
# the normal checkmarx.sh script has been called.
TEST_RECOMPRESSED_ZIP_FILE_PATH="$PDS_JOB_EXTRACTED_SOURCES_FOLDER/../recompressed"
cd "$TEST_RECOMPRESSED_ZIP_FILE_PATH"
unzip sourcecode.zip

# There must be a data.txt file inside - we create a sha256 for this file now
# (For the complete zip file it is not possible, because timestamps differ inside the zip file, so
# sha256 would be always different)

TEST_RECOMPRESSED_ZIP_DATA_FILENAME=""

if [ -f "./data-äüÖ.txt" ]; then
  TEST_RECOMPRESSED_ZIP_DATA_FILENAME_WITH_UMLAUTS="true"
  TEST_RECOMPRESSED_ZIP_DATA_TXT_SHA256=$(sha256sum ./data-äüÖ.txt)
else
  TEST_RECOMPRESSED_ZIP_DATA_FILENAME_WITH_UMLAUTS="false"
  TEST_RECOMPRESSED_ZIP_DATA_TXT_SHA256=$(sha256sum ./data.txt)
fi
dumpVariable "TEST_RECOMPRESSED_ZIP_DATA_FILENAME_WITH_UMLAUTS"

dumpVariable "TEST_RECOMPRESSED_ZIP_DATA_TXT_SHA256"