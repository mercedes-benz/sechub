#!/bin/bash
# SPDX-License-Identifier: MIT

source "$(dirname -- "$0")/../common-setup.sh"

echo "Start executing helper script"
echo "Please make sure you have started the integrationtest server with the IDE"
echo "Setting up CODE_SCAN Project for integrationtest server with mocked GOSEC Product"

initTestEnvironment

echo 'Starting test setup...'

test_project_name="test-gosec"
test_user1_name="web-ui-tester1"
test_user1_email="web-ui-tester1@example.org"

echo "- create project"
# Run the sechub-api.sh scripts with the necessary parameters
$SECHUB_API_SCRIPT project_create $test_project_name $SECHUB_USERID
$SECHUB_API_SCRIPT project_assign_user $test_project_name $SECHUB_USERID

echo "- setup project"
# Create and assign a mocked executor, the result will always be RED
$SECHUB_API_SCRIPT executor_create gosec-executor.json
$SECHUB_API_SCRIPT profile_create gosec-profile pds-gosec
$SECHUB_API_SCRIPT project_assign_profile $test_project_name gosec-profile

echo "- create test user: $test_user1_name"
# Create and assign a new test user which gains access to test project
$SECHUB_API_SCRIPT user_signup $test_user1_name $test_user1_email
$SECHUB_API_SCRIPT user_signup_accept $test_user1_name

echo "- assign test user: $test_user1_name to project: $test_project_name"
$SECHUB_API_SCRIPT project_assign_user $test_project_name $test_user1_name

# WEBSCAN (Optional)
# Create and assign a mocked executor, the result will always be RED
$SECHUB_API_SCRIPT executor_create owasp-zap-executor.json
$SECHUB_API_SCRIPT profile_create owaspzap-profile pds-owaspzap
$SECHUB_API_SCRIPT  project_assign_profile $TEST_PROJECT_NAME owaspzap-profile
$SECHUB_API_SCRIPT project_set_whitelist_uris $TEST_PROJECT_NAME https://example.org

sechub scan -project "$TEST_PROJECT_NAME"

echo "Finished setting up CODE_SCAN Project for integrationtest server with mocked Products"
