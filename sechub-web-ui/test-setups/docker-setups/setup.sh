#!/bin/bash
# SPDX-License-Identifier: MIT

source "$(dirname -- "$0")/../common-setup.sh"

echo "Start executing helper script"
echo "Please make sure you have started the Docker SecHub Server and Docker PDS Gosec and Gitleaks"
echo "Setting up Gosec and Gitleaks Project for Docker Server with real Products"

initTestEnvironment

echo 'Starting test setup...'

# setting up gosec
$SECHUB_SOLUTION_DIR/setup-pds/setup-gosec.sh
$SECHUB_API_SCRIPT project_assign_user test-gosec $SECHUB_USERID

# setting up gitleaks
$SECHUB_SOLUTION_DIR/setup-pds/setup-gitleaks.sh
$SECHUB_API_SCRIPT project_assign_user test-gitleaks $SECHUB_USERID

echo "Finished setting up Gosec and Gitleaks Project for Docker Server with real Products"