#!/bin/bash
# SPDX-License-Identifier: MIT

source "$(dirname -- "$0")/../common-setup.sh"

echo "Start executing helper script"
echo "Please make sure you have started the integrationtest server with the IDE"
echo "Setting up CODE_SCAN Project for integrationtest server with mocked GOSEC Product"

initTestEnvironment

echo 'Starting test setup...'

test_project_name="test-gosec"

# Run the sechub-api.sh scripts with the necessary parameters
$SECHUB_API_SCRIPT project_create $test_project_name $SECHUB_USERID
$SECHUB_API_SCRIPT project_assign_user $test_project_name $SECHUB_USERID

# Create and assign a mocked executor, the result will always be RED
$SECHUB_API_SCRIPT executor_create gosec-executor.json
$SECHUB_API_SCRIPT profile_create gosec-profile pds-gosec
$SECHUB_API_SCRIPT project_assign_profile $test_project_name gosec-profile

echo "Finished setting up CODE_SCAN Project for integrationtest server with mocked Products"
