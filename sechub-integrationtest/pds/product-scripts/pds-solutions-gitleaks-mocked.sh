#!/bin/bash 
# SPDX-License-Identifier: MIT

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh
echo "Current directory:"
pwd

echo "PDS solutions GITLEAKS mock starting"
cp "./../sechub-pds-solutions/gitleaks/docker/mocks/mock.sarif.json" "$PDS_JOB_RESULT_FILE"

warnMessage "mocked result"
infoMessage "product:gitleaks"

# Here we test the sechub wrapper secret validator application with the results of "mock.sarif.json"
export PDS_INTEGRATIONTEST_ENABLED=true
export TOOL_FOLDER=./../sechub-integrationtest/build/pds-tools

# Export the config file necessary for the sechub wrapper secret validator application
# Besides the config file the wrapper application will automatically use the PDS_JOB_RESULT_FILE,
# which is already available in this context
export SECRET_VALIDATOR_CONFIGFILE="./../sechub-pds-solutions/gitleaks/docker/sechub-wrapper-secretvalidation-config.json"

# Uses the original gitleaks.sh script from the pds gitleaks
# Since 'PDS_INTEGRATIONTEST_ENABLED=true' gitleaks will no be executed.
# The wrapper application starts with the 'integrationtest' profile and will not perform real web reuqests
source ./../sechub-pds-solutions/gitleaks/docker/scripts/gitleaks.sh
