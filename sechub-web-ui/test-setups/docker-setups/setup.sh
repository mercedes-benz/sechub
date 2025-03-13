#!/bin/bash
# SPDX-License-Identifier: MIT

echo "Start executing helper script"
echo "Please make sure you have started the Docker server and PDS Gosec and Gitleaks"
echo "Setting up Gosec and Gitleaks Project for Docker Server with real Products"

srcdir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )

cd $srcdir 

# Copy the .env file to the current directory
cp ../../.env .

# Source the .env file to load environment variables
set -a
source ./.env
set +a

echo "Using VITE .env to setup your user and apitoken"

# Export additional variables
export SECHUB_APITOKEN=${VITE_API_PASSWORD}
export SECHUB_USERID=${VITE_API_USER}
export SECHUB_SERVER=https://localhost:8443

echo 'Starting test setup...'

# setting up gosec
../../../sechub-solution/setup-pds/setup-gosec.sh
../../../sechub-developertools/scripts/sechub-api.sh project_assign_user test-gosec $SECHUB_USERID

# setting up gitleaks
../../../sechub-solution/setup-pds/setup-gitleaks.sh
../../../sechub-developertools/scripts/sechub-api.sh project_assign_user test-gitleaks $SECHUB_USERID

echo "Finished setting up Gosec and Gitleaks Project for Docker Server with real Products"