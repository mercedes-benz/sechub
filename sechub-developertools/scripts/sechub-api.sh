#!/bin/bash
# SPDX-License-Identifier: MIT

# ---------------------------------------------
# Shell front end to selected SecHub API calls
# ---------------------------------------------
# This can be handy for batch operations (e.g. creation or modification of multiple objects)
# or for using shell tools like grep to search the results
#
# SecHub Rest API documentation: https://mercedes-benz.github.io/sechub/latest/sechub-restapi.html

# Tip: Set SECHUB_SERVER, SECHUB_USERID and SECHUB_APITOKEN as environment variables

function usage {
  cat - <<EOF
Usage: `basename $0` [-p] [-y] [-s <sechub server url> [-u <sechub user>] [-a <sechub api token>] action [<action's parameters>]

Shell front end to selected SecHub API calls (https://mercedes-benz.github.io/sechub/latest/sechub-restapi.html).
Output will be beautified/colorized by piping json output through jq command (https://github.com/stedolan/jq)
unless you specify -p or -plain option. Option -y or -yes skips confirmation dialog when deleting items.

You are encouraged to set SECHUB_SERVER, SECHUB_USERID and SECHUB_APITOKEN as environment variables
so you can omit setting them via options which is better, because your secrets will not be revealed in the process list.

List of actions and mandatory parameters:
ACTION [PARAMETERS] - EXPLANATION
----------------------------------
alive_check - alive check (No user needed)
autocleanup_get - Get autocleanup setting
autocleanup_set <value> <time unit> - Update autocleanup setting. <time unit> is one of days, weeks, months, years
executor_create <json-file> - Create executor configuration from JSON file
executor_delete <executor-uuid> - Delete executor <executor-uuid>
executor_details <executor-uuid> - Show definition of executor <executor-uuid>
executor_list - List all existing executors (json format)
executor_update <executor-uuid> <json-file> - Update executor <executor-uuid> with <json-file> contents
job_approve <project-id> <job-uuid> - Approve a job <job-uuid> and mark it as ready to start
job_cancel <job-uuid> - Cancel job <job-uuid>
job_create <project-id> <json-file> - Create a new job for a project <project-id> from a SecHub configuration file <json-file> (JSON format)
job_get_report_spdx_json <project-id> <job-uuid> - Get SPDX JSON report for a specific job <job-uuid> and <project-id>
job_list_running - List running jobs (json format)
job_restart <job-uuid> - Restart/activate job <job-uuid>
job_restart_hard <job-uuid> - Run new backend scan of job <job-uuid>
job_status <project-id> <job-uuid> - Get status of job <job-uuid> in project <project-id> (json format)
job_upload_sourcecode <project-id> <job-uuid> <zip-file> - Upload source code <zip-file> for project <project-id> and job <job-uuid>
job_upload_binaries <project-id> <job-uuid> <tar-file> - Upload source code <tar-file> for project <project-id> and job <job-uuid>
profile_create <profile-id> <executor-uuid1>[,<executor-uuid2>...] [<description>]
               Create execution profile <profile-id> with named executors assigned; description optional
profile_delete <profile-id> - Delete execution profile <profile-id>
profile_details <profile-id> - Show details of execution profile <profile-id> (e.g. assinged executors)
profile_list - List all existing execution profiles (json format)
profile_update <profile-id> <executor-uuid1>[,<executor-uuid2>...] [<description>]
               Update execution profile <profile-id> with named executors assigned; description optional
project_assign_profile <project-id> <profile-id> - Assign execution profile <profile-id> to project <project-id>
project_assign_user <project-id> <user-id> - Assign user to project (allow scanning)
project_create <project-id> <owner> ["<project short description>"] - Create a new project. The short description is optional.
project_delete <project-id> - Delete project <project-id>
project_details <project-id> - Show owner, users, whitelist etc. of project <project-id>
project_details_all <project-id> - project_details plus assigned execution profiles
project_falsepositives_list <project-id> - Get defined false-positives for project <project-id> (json format)
project_list - List all projects (json format)
project_metadata_set <project-id> <key1>:<value1> [<key2>:<value2> ...] - define metadata for <project-id>
project_mockdata_list <project-id> - display defined mocked results (if server runs 'mocked-products')
project_mockdata_set <project-id> <code> <web> <infra> - define mocked results (possible values: RED YELLOW GREEN)
project_scan_list <project-id> - List scan jobs for project <project-id> (json format)
project_set_accesslevel <project-id> <accesslevel> - Set access level of project <project-id> to one of: full read_only none
project_set_owner <project-id> <owner> - Change owner of project <project-id> to <owner>
project_set_whitelist_uris <project-id> <uri1>[,<uri2>...] - Set whitelist uris for project <project-id>
project_unassign_profile <project-id> <profile-id> - Unassign execution profile <profile-id> from project <project-id>
project_unassign_user <project-id> <user-id> - Unassign user from project (revoke scanning)
scheduler_disable - Stop SecHub job scheduler
scheduler_enable - Continue SecHub job scheduler
scheduler_status - Get scheduler status
server_status - Get status entries of SecHub server like scheduler, jobs etc. (json format)
server_version - Print version of SecHub server
superadmin_grant <user-id> - Grant superadmin role to user <user-id>
superadmin_list - List all superadmin users (json format)
superadmin_revoke - Revoke superadmin role from user <user-id>
user_change_email <user-id> <new email address> - Update the email of user <user-id>
user_delete <user-id> - Delete user <user-id>
user_details <user-id> - List details of user <user-id> (json format)
user_list - List all users (json format)
user_list_open_signups - List all users waiting to get their signup accepted (json format)
user_reset_apitoken <email address> - Request new api token for <email address>
user_signup <new user-id> <email address> - Add <new user-id> with <email address> (needs to be accepted then)
user_signup_accept <new user-id> - Accept registration of <new user-id>
user_signup_decline <new user-id> - Decline registration of <new user-id>

EOF
}

#################################
# Generic helper functions

function are_you_sure {
  local INPUT
  if $YES ; then # Skip dialog?
    return 0
  fi

  echo -n " Are you sure? (y/n) "
  read INPUT
  case "$INPUT" in
    y|Y|yes|Yes|Yes) ;;
    *) echo "No action taken."
      exit 0
      ;;
  esac
}


function to_lower_case() {
	echo "$*" | tr [:upper:] [:lower:]
}


function generate_json_key_value {
  local key="$1"
  shift
  local value="$*"
  cat <<EOF
"$key": "$value"
EOF
}


function check_choice_parameter {
  local param="$1" ; shift
  local parameter_name="$1" ; shift
  local allowed_values="$*"

  check_parameter "$param" "$parameter_name"
  if ! $failed ; then
    local value="${!param}"
    local match=false
    for i in $allowed_values ; do
      [ "$value" = "$i" ] && match=true
    done
    if ! $match ; then
      echo "Required parameter $parameter_name is not one of: $allowed_values"
      failed=true
    fi
  fi
}


function check_parameter {
  local param="$1"
  local parameter_name="$2"
  if [ -z "${!param}" ] ; then
    echo "Required parameter $parameter_name is missing"
    failed=true
  fi
}


function check_trafficlight {
  local param="$1"
  local parameter_name="$2"
  case "${!param}" in
    RED|YELLOW|GREEN) ;;
    "") echo "Trafficlight value for $parameter_name not set"
      failed=true
      ;;
    *) echo "$parameter_name = \"${!param}\" is no trafficlight value (RED|YELLOW|GREEN)"
      failed=true
      ;;
  esac
}


function check_file {
  local file="$1"
  local parameter_name="$2"
  if [ -z "$file" ] ; then
    echo "$parameter_name is missing"
    failed=true
  elif [ ! -r "$file" ] ; then
    echo "File \"$file\" is not readable/existing. Please check."
    failed=true
  fi
}


function check_number {
  local param="$1"
  local parameter_name="$2"

  check_parameter "$param" "$parameter_name"
  if ! $failed ; then
    if [[ ! ${!param} =~ ^[0-9]+$ ]]; then
      echo "$parameter_name is not numeric."
      failed=true
    fi
  fi
}


function check_time_unit {
  local param="$1"
  local parameter_name="$2"

  check_parameter "$param" "$parameter_name"
  if ! $failed ; then
    if [[ ! ${!param} =~ ^(days?|weeks?|months?|years?)$ ]]; then
      echo "$parameter_name '${!param}' is not a valid time unit. Expected one of: days, weeks, months, years"
      failed=true
    fi
  fi
}


function generate_short_description {
  local action="$1"
  shift
  if [ $# -gt 0 ] ; then
    echo "$*"
  else
    echo "$action by sechub-api.sh at $TIMESTAMP"
  fi
}


#######################################
# SecHub api functions

function sechub_alive_check {
  echo "Alive status of $SECHUB_SERVER"
  curl $CURL_PARAMS -i -X GET "$SECHUB_SERVER/api/anonymous/check/alive" | $CURL_FILTER
}


function sechub_autocleanup_get {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/autoclean" | $RESULT_FILTER | $JSON_FORMATTER
}


function generate_autocleanup_data {
  local amount="$1"
  local unit="$2"
  cat <<EOF
{
  "cleanupTime": {
    "unit": "$unit",
    "amount": $amount
  }
}
EOF
}

function sechub_autocleanup_set {
  local JSON_DATA="$(generate_autocleanup_data $1 $2)"
  echo "Going to change autocleanup values. This may delete product results and scan reports."
  are_you_sure
  curl $CURL_PARAMS -i -X PUT -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/admin/config/autoclean" | $CURL_FILTER
}

function sechub_executor_create {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' -d "@$1" "$SECHUB_SERVER/api/admin/config/executor" | $RESULT_FILTER
  echo
}


function sechub_executor_details {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/executor/$1" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_executor_delete {
  echo "Executor \"$1\" will be deleted. This cannot be undone."
  are_you_sure
  curl $CURL_PARAMS -i -X DELETE -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/executor/$1" | $CURL_FILTER
}


function sechub_executor_update {
  curl $CURL_PARAMS -i -X PUT -H 'Content-Type: application/json' -d "@$2" "$SECHUB_SERVER/api/admin/config/executor/$1" | $CURL_FILTER
}


function sechub_executor_list {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/executors" | $RESULT_FILTER | jq '.executorConfigurations'
}


function sechub_job_cancel {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/jobs/cancel/$1"
}

function sechub_job_create {  
  local PROJECT_ID="$1"
  local JSON_FILE="$2"

  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' --data "@$JSON_FILE" "$SECHUB_SERVER/api/project/$PROJECT_ID/job" | $RESULT_FILTER | $JSON_FORMATTER
}

function sechub_job_approve {
  local PROJECT_ID="$1"
  local JOB_UUID="$2"
  
  curl $CURL_PARAMS -i -X PUT -H 'Content-Type: application/json' "$SECHUB_SERVER/api/project/$PROJECT_ID/job/$JOB_UUID/approve" | $CURL_FILTER
}

function sechub_job_restart {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/jobs/restart/$1"
}


function sechub_job_restart_hard {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/jobs/restart-hard/$1"
}

function sechub_job_get_report_spdx_json {
  local PROJECT_ID="$1"
  local JOB_UUID="$2"

  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/project/$PROJECT_ID/report/spdx/$JOB_UUID" 
}

function sechub_job_list_running {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/jobs/running" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_job_status {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/project/$1/job/$2" | $RESULT_FILTER | $JSON_FORMATTER
}

function sechub_job_upload_sourcecode {
  local PROJECT_ID="$1"
  local JOB_UUID="$2"
  local ZIP_FILE="$3"

  if [[ ! -f "$ZIP_FILE" ]] ; then
    echo "File \"$ZIP_FILE\" does not exist."
    exit 1
  fi

  local checkSum=$(sha256sum $ZIP_FILE | cut --delimiter=' ' --fields=1)

  curl $CURL_AUTH $CURL_PARAMS -i -X POST --header "Content-Type: multipart/form-data" \
    --form "file=@$ZIP_FILE" \
    --form "checkSum=$checkSum" \
    "$SECHUB_SERVER/api/project/$PROJECT_ID/job/$JOB_UUID/sourcecode" | $RESULT_FILTER

  if [[ "$?" == "0" ]] ; then
    echo "File \"$ZIP_FILE\" uploaded."
  else
    echo "Upload failed."
  fi
}

function sechub_job_upload_binaries {
  local PROJECT_ID="$1"
  local JOB_UUID="$2"
  local TAR_FILE="$3"

  if [[ ! -f "$TAR_FILE" ]] ; then
    echo "File \"$TAR_FILE\" does not exist."
    exit 1
  fi

  local checkSum=$(sha256sum $TAR_FILE | cut --delimiter=' ' --fields=1)
  local fileSize=$(ls -l "$TAR_FILE" | cut --delimiter=' ' --fields 5)

  curl $CURL_AUTH $CURL_PARAMS -i -X POST \
    --header "Content-Type: multipart/form-data" \
    --header "x-file-size: $fileSize" \
    --form "file=@$TAR_FILE" \
    --form "checkSum=$checkSum" \
    "$SECHUB_SERVER/api/project/$PROJECT_ID/job/$JOB_UUID/binaries" | $RESULT_FILTER

  if [[ "$?" == "0" ]] ; then
    echo "File \"$TAR_FILE\" uploaded."
  else
    echo "Upload failed."
  fi
}

function generate_sechub_profile_data {
  local EXECUTORS=$(echo $1 | awk -F',' '{ for (i = 1; i < NF; i++) { printf("{\"uuid\": \"%s\"}, ", $i) } printf ("{\"uuid\": \"%s\"}", $NF) }')
  shift
  local SHORT_DESCRIPTION="$*"
  cat <<EOF
{
  "configurations": [ $EXECUTORS ],
  "description": "$SHORT_DESCRIPTION",
  "enabled": true
}
EOF
}

function sechub_profile_create {
  local JSON_DATA="$(generate_sechub_profile_data $2 $3)"
  echo $JSON_DATA | $JSON_FORMATTER
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/admin/config/execution/profile/$1" | $CURL_FILTER
}


function sechub_profile_delete {
  echo "Execution profile \"$1\" will be deleted. This cannot be undone."
  are_you_sure
  curl $CURL_PARAMS -i -X DELETE -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/execution/profile/$1" | $CURL_FILTER
}


function sechub_profile_details {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/execution/profile/$1" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_profile_list {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/execution/profiles" | $RESULT_FILTER | jq '.executionProfiles'
}


function sechub_profile_update {
  local JSON_DATA="$(generate_sechub_profile_data $2 $3)"
  echo $JSON_DATA | $JSON_FORMATTER
  curl $CURL_PARAMS -i -X PUT -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/admin/config/execution/profile/$1" | $CURL_FILTER
}


function profile_short_description {
  local profile_id="$1"
  mapfile -t resultArray < <(sechub_profile_details $profile_id | jq '.enabled,.configurations[].name')
  local enabled=${resultArray[0]}
  local enabledState
  if [ "$enabled" = "true" ] ; then
    enabledState="enabled"
  else
    enabledState="disabled"
  fi
  local first_run="true"
  for i in "${resultArray[@]}" ; do
    if [ "$first_run" = "true" ] ; then
      echo "- \"$profile_id\" ($enabledState)"
      echo "  with executor configurations:"
      first_run="false"
    else
      echo "  + $i"
    fi
  done
}


function sechub_project_assign_profile {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/execution/profile/$2/project/$1" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_project_assign_user {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1/membership/$2" | $RESULT_FILTER | $JSON_FORMATTER
}


function generate_sechub_project_create_data {
  local PROJECT_ID="${1,,}"  # ,, converts to lowercase
  shift
  local OWNER="${1,,}"
  shift
  local SHORT_DESCRIPTION="$*"
  cat <<EOF
{
  "apiVersion":"$SECHUB_API_VERSION",
  "name":"$PROJECT_ID",
  "owner":"$OWNER",
  "description":"$SHORT_DESCRIPTION"
}
EOF
}

function sechub_project_create {
  local JSON_DATA="$(generate_sechub_project_create_data $1 $2 $3)"
  echo $JSON_DATA | $JSON_FORMATTER  # Show what is sent
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/admin/project" | $CURL_FILTER
}


function sechub_project_delete {
  echo "Project \"$1\" will be deleted. This cannot be undone."
  are_you_sure
  curl $CURL_PARAMS -i -X DELETE -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1" | $CURL_FILTER
}


function sechub_project_details {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_project_details_all {
  local project_id="$1"
  sechub_project_details $project_id

  echo "Assigned profiles:"
  sechub_profile_list | jq '.[].id' | sed 's/"//g' | while read profile_id ; do
    sechub_profile_details $profile_id | jq '.projectIds' | grep $project_id >/dev/null 2>&1
    if [ "$?" = "0" ] ; then
      # Print details about profile
      profile_short_description $profile_id
    fi
  done
}


function sechub_project_falsepositives_list {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/project/$1/false-positives" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_project_list {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/projects" | $RESULT_FILTER | $JSON_FORMAT_SORT
}


function sechub_project_metadata_set {
  local key
  local value
  local PROJECT_ID="${1,,}"  # ,, converts to lowercase
  shift
  local JSON_DATA="{\"apiVersion\": \"$SECHUB_API_VERSION\",\"metaData\":{"
  local arr=("$@")
  local first=true
  for i in "${arr[@]}" ; do
    if $first ; then
      first=false
    else
      JSON_DATA+=", "
    fi
    key=`echo $i | awk -F':' '{print $1}'`
    # The awk script enables that the separator ':' can also be part of the value and is passed through
    # e.g.:  "mykey:myvalue is:2" ; value will be "myvalue is:2"
    value=$(echo $i | awk -F':' '{ for (i = 2; i < NF; i++) { printf("%s:", $i) } print $NF }')
    JSON_DATA+="$(generate_json_key_value $key $value)"
  done
  JSON_DATA+="}}"
  echo $JSON_DATA | $JSON_FORMATTER  # Show what is sent
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/admin/project/$PROJECT_ID/metadata" | $CURL_FILTER
}


function sechub_project_mockdata_list {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/project/$1/mockdata" | $RESULT_FILTER | $JSON_FORMATTER
}


function generate_sechub_project_mockdata {
  cat <<EOF
{
  "apiVersion": "$SECHUB_API_VERSION",
  "codeScan": { "result": "$1" },
  "webScan": { "result": "$2" },
  "infraScan": { "result": "$3" }
}
EOF
}

function sechub_project_mockdata_set {
  local JSON_DATA="$(generate_sechub_project_mockdata $2 $3 $4)"
  echo $JSON_DATA | $JSON_FORMATTER  # Show what is sent
  curl $CURL_PARAMS -i -X PUT -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/project/$1/mockdata" | $CURL_FILTER
}


function sechub_project_scan_list {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1/scan/logs" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_project_set_accesslevel {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1/accesslevel/$2" | $CURL_FILTER
}


function sechub_project_set_owner {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1/owner/$2" | $CURL_FILTER
}


function generate_sechub_whitelist_data {
  local uri_list=""
  if [ -n "$1" ] ; then
    uri_list=$(echo $1 | awk -F',' '{ for (i = 1; i < NF; i++) { printf("\"%s\",", $i) } printf ("\"%s\"", $NF) }')
  fi
  cat <<EOF
{
  "apiVersion": "$SECHUB_API_VERSION",
  "whiteList": {
    "uris":[$uri_list]
  }
}
EOF
}


function sechub_project_set_whitelist_uris {
  local JSON_DATA="$(generate_sechub_whitelist_data $2)"
  echo $JSON_DATA | $JSON_FORMATTER
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/admin/project/$1/whitelist" | $CURL_FILTER
}


function sechub_project_unassign_profile {
  curl $CURL_PARAMS -i -X DELETE -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/execution/profile/$2/project/$1" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_project_unassign_user {
  curl $CURL_PARAMS -i -X DELETE -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1/membership/$2" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_scheduler_disable {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/scheduler/disable/job-processing" > /dev/null 2>&1
  sechub_scheduler_status
}


function sechub_scheduler_enable {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/scheduler/enable/job-processing" > /dev/null 2>&1
  sechub_scheduler_status
}


function sechub_scheduler_status {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/status" | $RESULT_FILTER | $JSON_FORMAT_SORT | jq '.[0]'
}


function sechub_server_status {
  # 1. Update status in admin domain
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/scheduler/status/refresh" > /dev/null 2>&1
  # 2. Display status
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/status" | $RESULT_FILTER | $JSON_FORMAT_SORT
}


function sechub_server_version {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: text/plain' "$SECHUB_SERVER/api/admin/info/version" | $RESULT_FILTER
}


function sechub_superadmin_grant {
  curl $CURL_PARAMS -i -X POST "$SECHUB_SERVER/api/admin/user/$1/grant/superadmin" | $CURL_FILTER
}


function sechub_superadmin_list {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/admins" | $RESULT_FILTER | $JSON_FORMAT_SORT
}


function sechub_superadmin_revoke {
  curl $CURL_PARAMS -i -X POST "$SECHUB_SERVER/api/admin/user/$1/revoke/superadmin" | $CURL_FILTER
}


function sechub_user_change_email {
  curl $CURL_PARAMS -i -X PUT "$SECHUB_SERVER/api/admin/user/$1/email/$2" | $CURL_FILTER
}


function sechub_user_delete {
  echo "User \"$1\" will be deleted. This cannot be undone."
  are_you_sure
  curl $CURL_PARAMS -i -X DELETE "$SECHUB_SERVER/api/admin/user/$1" | $CURL_FILTER
}


function sechub_user_details {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/user/$1" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_user_list {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/users" | $RESULT_FILTER | $JSON_FORMAT_SORT
}


function sechub_user_list_open_signups {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/signups" | $RESULT_FILTER | $JSON_FORMAT_SORT
}


function sechub_user_reset_apitoken {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/anonymous/refresh/apitoken/$1" | $CURL_FILTER
}


function generate_sechub_user_signup_data {
  cat <<EOF
{
  "apiVersion":"$SECHUB_API_VERSION",
  "userId":"$1",
  "emailAdress":"$2"
}
EOF
}

function sechub_user_signup {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' \
    -d "$(generate_sechub_user_signup_data $1 $2)" \
    "$SECHUB_SERVER/api/anonymous/signup" | $CURL_FILTER
}


function sechub_user_signup_accept {
  curl $CURL_PARAMS -i -X POST "$SECHUB_SERVER/api/admin/signup/accept/$1" | $CURL_FILTER
}


function sechub_user_signup_decline {
  curl $CURL_PARAMS -i -X DELETE "$SECHUB_SERVER/api/admin/signup/$1" | $CURL_FILTER
}

# -----------------------------
# main
failed=false
YES=false
SECHUB_API_VERSION="1.0"
NOFORMAT_PIPE="cat -"
RESULT_FILTER="tail -1"
TIMESTAMP=`date +"%Y-%m-%d %H:%M %Z"`
if which jq >/dev/null 2>&1 ; then
  JSON_FORMATTER="jq ."   # . is needed or pipeing the result is not possible
  JSON_FORMAT_SORT="jq sort"
else
  echo "### Hint: Install jq (https://github.com/stedolan/jq) to improve output." >&2  # appears only on stderr
  JSON_FORMATTER="$NOFORMAT_PIPE"
  JSON_FORMAT_SORT="$NOFORMAT_PIPE"
fi

# Parse command line options (everything starting with '-')
opt="$1"
while [[ "${opt:0:1}" == "-" ]] ; do
  case $opt in
  -a|-apitoken)
    SECHUB_APITOKEN="$2"
    shift 2
    ;;
  -d|-debug)
    # Plain output including header information from curl
    RESULT_FILTER="$NOFORMAT_PIPE"
    JSON_FORMATTER="$NOFORMAT_PIPE"
    JSON_FORMAT_SORT="$NOFORMAT_PIPE"
    shift
    ;;
  -h|-help)
    usage
    exit 0
    ;;
  -p|-plain)
    JSON_FORMATTER="$NOFORMAT_PIPE"
    JSON_FORMAT_SORT="$NOFORMAT_PIPE"
    shift
    ;;
  -s|-server)
    SECHUB_SERVER="$2"
    shift 2
    ;;
  -u|-user)
    SECHUB_USERID="$2"
    shift 2
    ;;
  -v|-verbose)
    echo "### Connecting as $SECHUB_USERID to $SECHUB_SERVER"
    shift
    ;;
  -y|-yes)
    YES=true
    shift
    ;;
  *)
    echo "Unknown option: \"$opt\""
    usage
    exit 1
    ;;
  esac
  opt="$1"
done

# Check if mandatory parameters are defined
for i in SECHUB_SERVER SECHUB_USERID SECHUB_APITOKEN ; do
  check_parameter "$i" "\$$i"
done
AUTH="$SECHUB_USERID:$SECHUB_APITOKEN"
CURL_PARAMS="-u $AUTH --silent --insecure --show-error"
CURL_FILTER="grep -i http/\|error"

action="$1" && shift
case "$action" in
  alive_check)
    $failed || sechub_alive_check
    ;;
  autocleanup_get)
    $failed || sechub_autocleanup_get
    ;;
  autocleanup_set)
    AUTOCLEANUP_VALUE="$1" ; check_number AUTOCLEANUP_VALUE '<value>'
    AUTOCLEANUP_UNIT=`to_lower_case "$2"` ; check_time_unit AUTOCLEANUP_UNIT '<time unit>'
    $failed || sechub_autocleanup_set $AUTOCLEANUP_VALUE $AUTOCLEANUP_UNIT
    ;;
  executor_create)
    EXECUTOR_JSONFILE="$1" ; check_file "$EXECUTOR_JSONFILE" '<json-file>'
    $failed || sechub_executor_create "$EXECUTOR_JSONFILE"
    ;;
  executor_delete)
    EXECUTOR_UUID="$1" ; check_parameter EXECUTOR_UUID '<executor-uuid>'
    $failed || sechub_executor_delete "$EXECUTOR_UUID"
    ;;
  executor_details)
    EXECUTOR_UUID="$1" ; check_parameter EXECUTOR_UUID '<executor-uuid>'
    $failed || sechub_executor_details "$EXECUTOR_UUID"
    ;;
  executor_list)
    $failed || sechub_executor_list
    ;;
  executor_update)
    EXECUTOR_UUID="$1" ; check_parameter EXECUTOR_UUID '<executor-uuid>'
    EXECUTOR_JSONFILE="$2" ; check_file "$EXECUTOR_JSONFILE" '<json-file>'
    $failed || sechub_executor_update "$EXECUTOR_UUID" "$EXECUTOR_JSONFILE"
    ;;
  job_approve)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    JOB_UUID="$2" ; check_parameter JOB_UUID '<job-uuid>'
    $failed || sechub_job_approve "$PROJECT_ID" "$JOB_UUID"
    ;;
  job_cancel)
    JOB_UUID="$1" ; check_parameter JOB_UUID '<job-uuid>'
    $failed || sechub_job_cancel "$JOB_UUID"
    ;;
  job_create)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    JSON_FILE="$2" ; check_parameter JSON_FILE '<json-file>'
    $failed || sechub_job_create "$PROJECT_ID" "$JSON_FILE"
    ;;
  job_get_report_spdx_json)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    JSON_FILE="$2" ; check_parameter JSON_FILE '<json-file>'
    $failed || sechub_job_get_report_spdx_json "$PROJECT_ID" "$JSON_FILE"
    ;;
  job_list_running)
    $failed || sechub_job_list_running
    ;;
  job_restart)
    JOB_UUID="$1" ; check_parameter JOB_UUID '<job-uuid>'
    $failed || sechub_job_restart "$JOB_UUID"
    ;;
  job_restart_hard)
    JOB_UUID="$1" ; check_parameter JOB_UUID '<job-uuid>'
    $failed || sechub_job_restart_hard "$JOB_UUID"
    ;;
  job_status)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    JOB_UUID="$2"   ; check_parameter JOB_UUID '<job-uuid>'
    $failed || sechub_job_status "$PROJECT_ID" "$JOB_UUID"
    ;;
  job_upload_sourcecode)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    JOB_UUID="$2"   ; check_parameter JOB_UUID '<job-uuid>'
    ZIP_FILE="$3"   ; check_parameter ZIP_FILE '<zip-file>'
    $failed || sechub_job_upload_sourcecode "$PROJECT_ID" "$JOB_UUID" "$ZIP_FILE"
    ;;
  job_upload_binaries)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    JOB_UUID="$2"   ; check_parameter JOB_UUID '<job-uuid>'
    TAR_FILE="$3"   ; check_parameter TAR_FILE '<tar-file>'
    $failed || sechub_job_upload_binaries "$PROJECT_ID" "$JOB_UUID" "$TAR_FILE"
    ;;
  profile_create)
    PROFILE_ID="$1" ; check_parameter PROFILE_ID '<profile-id>'
    EXECUTORS="$2" ; check_parameter EXECUTORS '<executor-uuid1>[,<executor-uuid2>...]'
    shift ; shift
    PROFILE_SHORT_DESCRIPTION="$(generate_short_description Created $*)"
    $failed || sechub_profile_create "$PROFILE_ID" "$EXECUTORS" "$PROFILE_SHORT_DESCRIPTION"
    ;;
  profile_delete)
    PROFILE_ID="$1" ; check_parameter PROFILE_ID '<profile-id>'
    $failed || sechub_profile_delete "$PROFILE_ID"
    ;;
  profile_details)
    PROFILE_ID="$1" ; check_parameter PROFILE_ID '<profile-id>'
    $failed || sechub_profile_details "$PROFILE_ID"
    ;;
  profile_list)
    $failed || sechub_profile_list
    ;;
  profile_update)
    PROFILE_ID="$1" ; check_parameter PROFILE_ID '<profile-id>'
    EXECUTORS="$2" ; check_parameter EXECUTORS '<executor-uuid1>[,<executor-uuid2>...]'
    shift ; shift
    PROFILE_SHORT_DESCRIPTION="$(generate_short_description Updated $*)"
    $failed || sechub_profile_update "$PROFILE_ID" "$EXECUTORS" "$PROFILE_SHORT_DESCRIPTION"
    ;;
  project_assign_profile)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    PROFILE_ID="$2" ; check_parameter PROFILE_ID '<profile-id>'
    $failed || sechub_project_assign_profile "$PROJECT_ID" "$PROFILE_ID"
    ;;
  project_assign_user)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    SECHUB_USER="$2" ; check_parameter SECHUB_USER '<user-id>'
    $failed || sechub_project_assign_user "$PROJECT_ID" "$SECHUB_USER"
    ;;
  project_create)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    PROJECT_OWNER="$2" ; check_parameter PROJECT_OWNER '<owner>'
    shift ; shift
    PROJECT_SHORT_DESCRIPTION="$(generate_short_description Created $*)"
    $failed || sechub_project_create "$PROJECT_ID" "$PROJECT_OWNER" "$PROJECT_SHORT_DESCRIPTION"
    ;;
  project_delete)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    $failed || sechub_project_delete "$PROJECT_ID"
    ;;
  project_details)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    $failed || sechub_project_details "$PROJECT_ID"
    ;;
  project_details_all)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    $failed || sechub_project_details_all "$PROJECT_ID"
    ;;
  project_falsepositives_list)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    $failed || sechub_project_falsepositives_list "$PROJECT_ID"
    ;;
  project_list)
    $failed || sechub_project_list
    ;;
  project_metadata_set)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    METADATA="$2" ; check_parameter METADATA '<key1>:<value1>' # We expect at least one pair
    shift
    for item in "$@"; do
      METADATA_LIST+=("${item}")
    done
    $failed || sechub_project_metadata_set "$PROJECT_ID" "${METADATA_LIST[@]}"
    ;;
  project_mockdata_list)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    $failed || sechub_project_mockdata_list "$PROJECT_ID"
    ;;
  project_mockdata_set)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    TRAFFICLIGHT_CODESCAN="$2" ; check_trafficlight TRAFFICLIGHT_CODESCAN '<code>'
    TRAFFICLIGHT_WEBSCAN="$3" ; check_trafficlight TRAFFICLIGHT_WEBSCAN '<web>'
    TRAFFICLIGHT_INFRASCAN="$4" ; check_trafficlight TRAFFICLIGHT_INFRASCAN '<infra>'
    $failed || sechub_project_mockdata_set "$PROJECT_ID" "$TRAFFICLIGHT_CODESCAN" "$TRAFFICLIGHT_WEBSCAN" "$TRAFFICLIGHT_INFRASCAN"
    ;;
  project_scan_list)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    $failed || sechub_project_scan_list "$PROJECT_ID"
    ;;
  project_set_accesslevel)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    PROJECT_ACCESSLEVEL="$2" ; check_choice_parameter PROJECT_ACCESSLEVEL '<accesslevel>' full read_only none
    $failed || sechub_project_set_accesslevel "$PROJECT_ID" "$PROJECT_ACCESSLEVEL"
    ;;
  project_set_owner)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    PROJECT_OWNER="$2" ; check_parameter PROJECT_OWNER '<owner>'
    $failed || sechub_project_set_owner "$PROJECT_ID" "$PROJECT_OWNER"
    ;;
  project_set_whitelist_uris)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    WHITELIST_URIS="$2" # No check - we also accept an empty parameter -> clear whitelist
    $failed || sechub_project_set_whitelist_uris "$PROJECT_ID" "$WHITELIST_URIS"
    ;;
  project_unassign_profile)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    PROFILE_ID="$2" ; check_parameter PROFILE_ID '<profile-id>'
    $failed || sechub_project_unassign_profile "$PROJECT_ID" "$PROFILE_ID"
    ;;
  project_unassign_user)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    SECHUB_USER="$2" ; check_parameter SECHUB_USER '<user-id>'
    $failed || sechub_project_unassign_user "$PROJECT_ID" "$SECHUB_USER"
    ;;
  scheduler_disable)
    $failed || sechub_scheduler_disable
    ;;
  scheduler_enable)
    $failed || sechub_scheduler_enable
    ;;
  scheduler_status)
    $failed || sechub_scheduler_status
    ;;
  server_status)
    $failed || sechub_server_status
    ;;
  server_version)
    $failed || sechub_server_version
    ;;
  superadmin_grant)
    SECHUB_USER="$1" ; check_parameter SECHUB_USER '<user-id>'
    $failed || sechub_superadmin_grant "$SECHUB_USER"
    ;;
  superadmin_list)
    $failed || sechub_superadmin_list
    ;;
  superadmin_revoke)
    SECHUB_USER="$1" ; check_parameter SECHUB_USER '<user-id>'
    $failed || sechub_superadmin_revoke "$SECHUB_USER"
    ;;
  user_change_email)
    SECHUB_USER="$1" ; check_parameter SECHUB_USER '<user-id>'
    SECHUB_NEW_EMAIL="$2" ; check_parameter SECHUB_NEW_EMAIL '<new email address>'
    $failed || sechub_user_change_email "$SECHUB_USER" "$SECHUB_NEW_EMAIL"
    ;;
  user_delete)
    SECHUB_USER="$1" ; check_parameter SECHUB_USER '<user-id>'
    $failed || sechub_user_delete "$SECHUB_USER"
    ;;
  user_details)
    SECHUB_USER="$1" ; check_parameter SECHUB_USER '<user-id>'
    $failed || sechub_user_details "$SECHUB_USER"
    ;;
  user_list)
    $failed || sechub_user_list
    ;;
  user_list_open_signups)
    $failed || sechub_user_list_open_signups
    ;;
  user_reset_apitoken)
    SECHUB_EMAIL="$1" ; check_parameter SECHUB_EMAIL '<email address>'
    $failed || sechub_user_reset_apitoken "$SECHUB_EMAIL"
    ;;
  user_signup)
    SECHUB_USER="$1" ; check_parameter SECHUB_USER '<new user-id>'
    SECHUB_EMAIL="$2" ; check_parameter SECHUB_EMAIL '<email address>'
    $failed || sechub_user_signup "$SECHUB_USER" "$SECHUB_EMAIL"
    ;;
  user_signup_decline)
    SECHUB_USER="$1" ; check_parameter SECHUB_USER '<new user-id>'
    $failed || sechub_user_signup_decline "$SECHUB_USER"
    ;;
  user_signup_accept)
    SECHUB_USER="$1" ; check_parameter SECHUB_USER '<new user-id>'
    $failed || sechub_user_signup_accept "$SECHUB_USER"
    ;;
  ""|help)
    usage
    ;;
  *)
  echo "Unknown action: \"$action\""
  usage
  exit 1
  ;;
esac
# add missing newline if not formatted
[ "$JSON_FORMATTER" == "$NOFORMAT_PIPE" ] && echo ""

# failed?
if $failed ; then
  exit 1
fi
