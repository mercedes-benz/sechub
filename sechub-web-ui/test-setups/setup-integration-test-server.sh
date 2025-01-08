#!/bin/bash
# SPDX-License-Identifier: MIT
export SECHUB_APITOKEN='';
export SECHUB_USERID=;
export SECHUB_SERVER=https://localhost:8443;
export TEST_PROJECT_NAME="test-gosec"

../sechub-developertools/scripts/sechub-api.sh project_create $TEST_PROJECT_NAME $SECHUB_USERID
../sechub-developertools/scripts/sechub-api.sh project_assign_user $TEST_PROJECT_NAME $SECHUB_USERID

# creates and assigns a mocked executor, the result will always be RED
../sechub-developertools/scripts/sechub-api.sh executor_create gosec-executor.json
../sechub-developertools/scripts/sechub-api.sh profile_create gosec-profile pds-gosec
../sechub-developertools/scripts/sechub-api.sh project_assign_profile $TEST_PROJECT_NAME gosec-profile
