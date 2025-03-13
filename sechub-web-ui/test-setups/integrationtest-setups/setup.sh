#!/bin/bash
# SPDX-License-Identifier: MIT

echo "Start executing helper script"
echo "Please make sure you have started the integrationtest server with the IDE"
echo "Setting up CODE_SCAN Project for integrationtest server with mocked GOSEC Product"

srcdir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )

cd $srcdir 


# Copy the .env file to the current directory
if [ -e x.txt ]
then
    echo "Using existsing .env file"
else
    echo "Coping .env file from web-ui directory"
    cp ../../.env .
fi

# Source the .env file to load environment variables
set -a
source ./.env
set +a

echo "Using VITE .env to setup your user and apitoken"

# Export additional variables
export SECHUB_APITOKEN=${VITE_API_PASSWORD}
export SECHUB_USERID=${VITE_API_USER}
export SECHUB_SERVER=https://localhost:8443
export TEST_PROJECT_NAME="test-gosec"

echo 'Starting test setup...'

# Run the sechub-api.sh scripts with the necessary parameters
../../../sechub-developertools/scripts/sechub-api.sh project_create $TEST_PROJECT_NAME $SECHUB_USERID
../../../sechub-developertools/scripts/sechub-api.sh project_assign_user $TEST_PROJECT_NAME $SECHUB_USERID

# Create and assign a mocked executor, the result will always be RED
../../sechub-developertools/scripts/sechub-api.sh executor_create gosec-executor.json
../../sechub-developertools/scripts/sechub-api.sh profile_create gosec-profile pds-gosec
../../sechub-developertools/scripts/sechub-api.sh project_assign_profile $TEST_PROJECT_NAME gosec-profile

echo "Finished setting up CODE_SCAN Project for integrationtest server with mocked Products"
