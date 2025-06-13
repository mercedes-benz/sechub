#!/bin/bash
# SPDX-License-Identifier: MIT

source "$(dirname -- "$0")/../common-setup.sh"

echo "Start executing helper script"
echo "Please make sure you have started the integrationtest server with the IDE"
echo "Setting up CODE_SCAN Project for integrationtest server with mocked GOSEC Product"

initTestEnvironment

echo "Starting test setup..."
echo "- Working directory: $(pwd)"

test_gosec_kics_and_gitleaks_project_name="test-gosec-kics-and-gitleaks"
test_checkmarx_project_name="test-checkmarx"
test_zap_project_name="test-zap"
test_kics_project_name="test-kics"
test_only_owned_name="test-owned-only"

test_user1_name="web-ui-tester1"
test_user1_email="web-ui-tester1@example.org"

# Second testuser that can be alse created with keycloak (without json error)
test_user2_name="testuser"
test_user2_email="testuser@sechub.com"

$SECHUB_API_SCRIPT alive_check

echo "- create projects"
# Run the sechub-api.sh scripts with the necessary parameters
$SECHUB_API_SCRIPT project_create $test_gosec_kics_and_gitleaks_project_name $SECHUB_USERID
$SECHUB_API_SCRIPT project_create $test_checkmarx_project_name $SECHUB_USERID
$SECHUB_API_SCRIPT project_create $test_zap_project_name $SECHUB_USERID
$SECHUB_API_SCRIPT project_create $test_kics_project_name $SECHUB_USERID
$SECHUB_API_SCRIPT project_create $test_only_owned_name $SECHUB_USERID

echo "- create test user: '$test_user1_name'"
# Create and assign a new test user which gains access to test project
$SECHUB_API_SCRIPT user_signup $test_user1_name $test_user1_email
$SECHUB_API_SCRIPT user_signup_accept $test_user1_name

echo "- create test user: '$test_user2_name'"
$SECHUB_API_SCRIPT user_signup $test_user2_name $test_user2_email
$SECHUB_API_SCRIPT user_signup_accept $test_user2_name

echo "- assign test user: $test_user1_name to projects"
$SECHUB_API_SCRIPT project_assign_user $test_checkmarx_project_name $test_user1_name
$SECHUB_API_SCRIPT project_assign_user $test_gosec_kics_and_gitleaks_project_name $test_user1_name
$SECHUB_API_SCRIPT project_assign_user $test_zap_project_name $test_user1_name
$SECHUB_API_SCRIPT project_assign_user $test_zap_test_kics_project_name $test_user1_name
$SECHUB_API_SCRIPT project_assign_user $test_zap_test_kics_project_name $test_user2_name
$SECHUB_API_SCRIPT project_assign_user $test_only_owned_name $test_user1_name
$SECHUB_API_SCRIPT project_unassign_user $test_only_owned_name $SECHUB_USERID

# Make your user to non-admin user (e.g. for owner-only ui - normally not necessary)
$SECHUB_API_SCRIPT superadmin_grant $test_user1_name
$SECHUB_API_SCRIPT superadmin_revoke $SECHUB_USERID

echo "- setup project '$test_gosec_kics_and_gitleaks_project_name' for codescan with gosec mock, iac with kics and secret scan with gitleaks"
# Create and assign a mocked executor, the result will always be RED
$SECHUB_API_SCRIPT executor_create ./executors/gosec-executor.json
$SECHUB_API_SCRIPT profile_create gosec-profile pds-gosec
$SECHUB_API_SCRIPT project_assign_profile $test_gosec_kics_and_gitleaks_project_name gosec-profile

# Secretscan Gitleaks
echo "- setup project '$test_gosec_kics_and_gitleaks_project_name' for secretscan with gitleaks mock"
$SECHUB_API_SCRIPT executor_create ./executors/gitleaks-executor.json
$SECHUB_API_SCRIPT profile_create gitleaks-profile pds-gitleaks
$SECHUB_API_SCRIPT project_assign_profile $test_gosec_kics_and_gitleaks_project_name gitleaks-profile

# Codescan checkmarx
echo "- setup project '$test_checkmarx_project_name' for codescan with checkmarx mock"
$SECHUB_API_SCRIPT executor_create ./executors/checkmarx-executor.json
$SECHUB_API_SCRIPT profile_create checkmarx-profile pds-checkmarx
$SECHUB_API_SCRIPT project_assign_profile $test_checkmarx_project_name checkmarx-profile

# Iacscan kics
echo "- setup project '$test_kics_project_name' for iacscan with kics mock"
$SECHUB_API_SCRIPT executor_create ./executors/kics-executor.json
$SECHUB_API_SCRIPT profile_create kics-profile pds-kics
$SECHUB_API_SCRIPT project_assign_profile $test_kics_project_name kics-profile
$SECHUB_API_SCRIPT project_assign_profile $test_gosec_kics_and_gitleaks_project_name kics-profile

# WebScan Zap 
echo "- setup project '$test_zap_project_name' for webscan with ZAP mock"
$SECHUB_API_SCRIPT executor_create ./executors/zap-executor.json
$SECHUB_API_SCRIPT profile_create zap-profile pds-zap
$SECHUB_API_SCRIPT project_assign_profile $test_zap_project_name zap-profile
$SECHUB_API_SCRIPT project_set_whitelist_uris $test_zap_project_name https://example.org

#------------------------------------------
# Execute scans
#------------------------------------------
function executeScan(){
    project_name=$1
    config_file=$2

    echo "- executing job with config: '$config_file' for project: '$project_name'"
    sechub -wait 1 -project "$project_name" -configfile ./configurations/$config_file scanAsync

}
export SECHUB_QUIET=true
export SECHUB_DEBUG=false

executeScan "$test_gosec_kics_and_gitleaks_project_name" "sechub-code-iac-and-secret-scan.json"
executeScan "$test_checkmarx_project_name" "sechub-codescan.json"
executeScan "$test_zap_project_name" "sechub-webscan.json"
executeScan "$test_kics_project_name" "sechub-iacscan.json"
