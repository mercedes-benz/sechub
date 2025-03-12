#!/bin/bash
# SPDX-License-Identifier: MIT

# Copy the .env file to the current directory
cp ../.env .

# Source the .env file to load environment variables
set -a
source ./.env
set +a

echo "Using VITE .env to setup your user and apitoken"

# Export additional variables
export SECHUB_APITOKEN=${VITE_API_PASSWORD}
export SECHUB_USERID=${VITE_API_USERNAME}
export SECHUB_SERVER=https://localhost:8443
export TEST_PROJECT_NAME="test-gosec"

echo 'Starting test setup...'

# Run the sechub-api.sh scripts with the necessary parameters
../../sechub-developertools/scripts/sechub-api.sh project_create $TEST_PROJECT_NAME $SECHUB_USERID
../../sechub-developertools/scripts/sechub-api.sh project_assign_user $TEST_PROJECT_NAME $SECHUB_USERID

# Create and assign a mocked executor, the result will always be RED
../../sechub-developertools/scripts/sechub-api.sh executor_create gosec-executor.json
../../sechub-developertools/scripts/sechub-api.sh profile_create gosec-profile pds-gosec
../../sechub-developertools/scripts/sechub-api.sh project_assign_profile $TEST_PROJECT_NAME gosec-profile