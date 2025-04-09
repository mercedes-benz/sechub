#!/bin/bash
# SPDX-License-Identifier: MIT

source "$(dirname -- "$0")/../common-setup.sh"

echo "Start executing helper script"
echo "Please make sure you have started the integrationtest server with the IDE"
echo "Setting up CODE_SCAN Project for integrationtest server with mocked GOSEC Product"

initTestEnvironment

echo "Starting test setup..."
echo "- Working directory: $(pwd)"

test_gosec_and_gitleaks_project_name="test-gosec-and-gitleaks"
test_checkmarx_project_name="test-checkmarx"
test_zap_project_name="test-zap"

test_user1_name="web-ui-tester1"
test_user1_email="web-ui-tester1@example.org"

$SECHUB_API_SCRIPT alive_check

echo "- create projects"
# Run the sechub-api.sh scripts with the necessary parameters
$SECHUB_API_SCRIPT project_create $test_gosec_and_gitleaks_project_name $SECHUB_USERID
$SECHUB_API_SCRIPT project_create $test_checkmarx_project_name $SECHUB_USERID
$SECHUB_API_SCRIPT project_create $test_zap_project_name $SECHUB_USERID

echo "- create test user: '$test_user1_name'"
# Create and assign a new test user which gains access to test project
$SECHUB_API_SCRIPT user_signup $test_user1_name $test_user1_email
$SECHUB_API_SCRIPT user_signup_accept $test_user1_name

echo "- assign test user: $test_user1_name to projects"
$SECHUB_API_SCRIPT project_assign_user $test_checkmarx_project_name $test_user1_name
$SECHUB_API_SCRIPT project_assign_user $test_gosec_and_gitleaks_project_name $test_user1_name
$SECHUB_API_SCRIPT project_assign_user $test_zap_project_name $test_user1_name


echo "- setup project '$test_gosec_and_gitleaks_project_name' for codescan with gosec mock"
# Create and assign a mocked executor, the result will always be RED
$SECHUB_API_SCRIPT executor_create ./executors/gosec-executor.json
$SECHUB_API_SCRIPT profile_create gosec-profile pds-gosec
$SECHUB_API_SCRIPT project_assign_profile $test_gosec_and_gitleaks_project_name gosec-profile

# Secretscan Gitleaks
echo "- setup project '$test_gosec_and_gitleaks_project_name' for secretscan with gitleaks mock"
$SECHUB_API_SCRIPT executor_create ./executors/gitleaks-executor.json
$SECHUB_API_SCRIPT profile_create gitleaks-profile pds-gitleaks
$SECHUB_API_SCRIPT project_assign_profile $test_gosec_and_gitleaks_project_name gitleaks-profile

# Secretscan checkmarx
echo "- setup project '$test_checkmarx_project_name' for codescan with checkmarx mock"
$SECHUB_API_SCRIPT executor_create ./executors/checkmarx-executor.json
$SECHUB_API_SCRIPT profile_create checkmarx-profile pds-checkmarx
$SECHUB_API_SCRIPT project_assign_profile $test_checkmarx_project_name checkmarx-profile

# WebScan Zap 
echo "- setup project '$test_zap_project_name' for webscan with ZAP mock"
$SECHUB_API_SCRIPT executor_create ./executors/zap-executor.json
$SECHUB_API_SCRIPT profile_create zap-profile pds-zap
$SECHUB_API_SCRIPT project_assign_profile $test_zap_project_name zap-profile
$SECHUB_API_SCRIPT project_set_whitelist_uris $test_zap_project_name https://example.org

#---------------------
# Execute scans
#---------------------
function executeScan(){
    project_name=$1
    config_file=$2

    echo "- executing job with config: '$config_file' for project: '$project_name'"
    sechub -wait 1 -project "$project_name" -configfile ./configurations/$config_file scanAsync

}
export SECHUB_QUIET=true
export SECHUB_DEBUG=false

executeScan "$test_gosec_and_gitleaks_project_name" "sechub-codescan-and-secretscan.json"
executeScan "$test_checkmarx_project_name" "sechub-codescan.json"
executeScan "$test_zap_project_name" "sechub-webscan.json"
