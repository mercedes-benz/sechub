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
Output will be improved by piping json output through jq command (https://github.com/stedolan/jq)
unless you specify -p or -plain option. Option -y or -yes skips confirmation dialog when deleting items.

You are encouraged to set SECHUB_SERVER, SECHUB_USERID and SECHUB_APITOKEN as environment variables
so you can omit setting them via options which is better, because your secrets will not be revealed in the process list.

List of actions and mandatory parameters:
ACTION [PARAMETERS] - EXPLANATION
----------------------------------
alive_check - alive check (No user needed)
asset_file_create <asset id> <file> - Upload <file> for <asset id>. Asset will be created if not existing.
asset_file_delete <asset id> <file> - Delete uploaded <file> for <asset id>
asset_file_download <asset id> <file> - Download <file> from <asset id>
asset_list - List all existing assets
asset_details <asset id> - Show details of <asset id>
asset_delete <asset id> - Delete <asset id> and all its assigned files
autocleanup_get - Get autocleanup setting
autocleanup_set <value> <time unit> - Update autocleanup setting. <time unit> is one of days, weeks, months, years
executor_create <json-file> - Create executor configuration from JSON file
executor_delete <executor uuid or name> - Delete executor <executor uuid or name>
executor_details <executor uuid or name> - Show definition of executor <executor uuid or name>
executor_list - List all existing executors (json format)
executor_update <executor-uuid uuid or name> <json-file> - Update executor <executor-uuid uuid or name> with <json-file> contents
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
profile_create <profile-id> <executor1 uuid or name>[,<executor2 uuid or name>...] [<description>]
               Create execution profile <profile-id> with named executors assigned; description optional
profile_delete <profile-id> - Delete execution profile <profile-id>
profile_details <profile-id> - Show details of execution profile <profile-id> (e.g. assinged executors)
profile_list - List all existing execution profiles (json format)
profile_update <profile-id> <executor1 uuid or name>[,<executor2 uuid or name>...] [<description>]
               Update execution profile <profile-id> with named executors assigned; description optional
project_assign_profile <project-id> <profile-id> - Assign execution profile <profile-id> to project <project-id>
project_assign_template <project-id> <template-id> - Assign <template-id> to project <project-id>
project_assign_user <project-id> <user-id> - Assign <user-id> to project (allow scanning)
project_create <project-id> <owner> ["<project short description>"] - Create a new project. The short description is optional.
project_delete <project-id> - Delete project <project-id>
project_details <project-id> - Show owner, users, whitelist etc. of project <project-id>
project_details_all <project-id> - project_details plus assigned execution profiles of project <project-id>
project_falsepositives_list <project-id> - Get defined false-positives for project <project-id> (json format)
project_joblist <project-id> <size> <page> - List scan jobs of <project-id> (json format). Page# <page> of size <size>
project_list - List all projects (json format)
project_metadata_set <project-id> <key1>:<value1> [<key2>:<value2> ...] - define metadata for <project-id>
project_mockdata_list <project-id> - display defined mocked results (if server runs 'mocked-products')
project_mockdata_set <project-id> <code> <web> <infra> - define mocked results (possible values: RED YELLOW GREEN)
project_scan_list <project-id> - List scan jobs for project <project-id> (json format)
project_set_accesslevel <project-id> <accesslevel> - Set access level of project <project-id> to one of: full read_only none
project_set_owner <project-id> <owner> - Change owner of project <project-id> to <owner>
project_set_whitelist_uris <project-id> <uri1>[,<uri2>...] - Set whitelist uris for project <project-id>
project_unassign_profile <project-id> <profile-id> - Unassign execution profile <profile-id> from project <project-id>
project_unassign_template <project-id> <template-id> - Unassign <template-id> from project <project-id>
project_unassign_user <project-id> <user-id> - Unassign <user-id> from project (revoke scanning)
scheduler_disable - Stop SecHub job scheduler
scheduler_enable - Continue SecHub job scheduler
scheduler_status - Get scheduler status
server_encryption_rotate <algorithm> var=<env-var> - Change server encryption to secret in <env-var>
server_encryption_status - Get current status of encryption (json format)
server_info - Print infos about SecHub server (json format)
server_status - Get status entries of SecHub server like scheduler, jobs etc. (json format)
server_version - Print version of SecHub server
superadmin_grant <user-id> - Grant superadmin role to <user-id>
superadmin_list - List all superadmin users (json format)
superadmin_revoke <user-id> - Revoke superadmin role from <user-id>
template_create <template-id> <json file> - Create or update <template-id> with <json file>
template_delete <template-id> - Delete <template-id>
template_details <template-id> - Show details of <template-id>
template_list - List all existing templates
user_change_email <user-id> <new email address> - Update the email of <user-id>
user_delete <user-id> - Delete <user-id>
user_details <user-id or email> - List details of user <user-id or email> (json format)
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

function check_executable_is_installed(){
    executable="$1"
    exe_path=`which $executable`
    if [ ! -x "$exe_path" ] ; then
        echo "FATAL: Mandatory executable \"$executable\" not found in PATH. Please install."
        exit 1
    fi
}


function curl_with_sechub_auth {
  # Don't reveal secrets in the curl process
  printf "user = $SECHUB_USERID:$SECHUB_APITOKEN\n" | curl --config - $CURL_PARAMS "$@"
}


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


function is_uuid {
  # Example: 806b1f9a-f31c-4238-8201-ce9e9fc06b28
  if [[ $1 =~ ^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$ ]]; then
    return 0
  else
    return 1
  fi
}


function get_executor_uuid_from_name {
  local uuid
  curl_with_sechub_auth -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/executors" \
    | jq -r --arg executorname "$1" '.executorConfigurations | map(select(.name == $executorname)) | .[].uuid'
}


function get_executor_uuid {
  local uuid
  if is_uuid "$1" ; then
    uuid="$1"
  else
    uuid="$(get_executor_uuid_from_name $1)"
    if ! is_uuid "$uuid" ; then
      echo "Executor with name \"$1\" not found or not unique." >&2
      exit 1
    fi
  fi
  echo $uuid
}


#######################################
# SecHub api functions

function sechub_alive_check {
  echo "Alive status of $SECHUB_SERVER"
  curl $CURL_PARAMS -i -X GET "$SECHUB_SERVER/api/anonymous/check/alive" | $CURL_FILTER
}


function sechub_asset_file_create {
  local asset_id="$1"
  local asset_file="$2"
  local asset_file_sha256=`sha256sum "$asset_file" | cut -d ' ' -f 1`
  curl_with_sechub_auth -i -X POST -H 'Content-Type: multipart/form-data' \
    --form "file=@$asset_file" \
    "$SECHUB_SERVER/api/admin/asset/$asset_id/file?checkSum=$asset_file_sha256" | $CURL_FILTER
}


function sechub_asset_file_delete {
  local asset_id="$1"
  local asset_file="$2"
  echo "File \"$asset_file\" will be deleted from asset \"$asset_id\". This cannot be undone."
  are_you_sure
  curl_with_sechub_auth -i -X DELETE -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/asset/$asset_id/file/$asset_file" | $CURL_FILTER
}


function sechub_asset_file_download {
  local asset_id="$1"
  local asset_file="$2"
  if [ -f "$asset_file" ] ; then
    echo "Your local file \"$asset_file\" will be overridden."
    are_you_sure
  fi
  curl_with_sechub_auth -X GET -H 'Content-Type: application/json' \
    "$SECHUB_SERVER/api/admin/asset/$asset_id/file/$asset_file" \
    --output "$asset_file" \
    --dump-header - | $CURL_FILTER
}


function sechub_asset_delete {
  local asset_id="$1"
  echo "Asset \"$asset_id\" will be deleted including its files. This cannot be undone."
  are_you_sure
  curl_with_sechub_auth -i -X DELETE -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/asset/$asset_id" | $CURL_FILTER
}


function sechub_asset_details {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/asset/$1/details" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_asset_list {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/asset/ids" | $RESULT_FILTER | $JSON_FORMAT_SORT
}


function sechub_autocleanup_get {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/autoclean" | $RESULT_FILTER | $JSON_FORMATTER
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
  curl_with_sechub_auth -i -X PUT -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/admin/config/autoclean" | $CURL_FILTER
}


function sechub_executor_create {
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' -d "@$1" "$SECHUB_SERVER/api/admin/config/executor" | $RESULT_FILTER
  echo
}


function sechub_executor_details {
  local uuid
  uuid="$(get_executor_uuid $1)"
  is_uuid "$uuid" && curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/executor/$uuid" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_executor_delete {
  local uuid
  uuid="$(get_executor_uuid $1)"
  if ! is_uuid "$uuid" ; then
    exit 1
  fi
  echo "Executor \"$1\" will be deleted. This cannot be undone."
  are_you_sure
  curl_with_sechub_auth -i -X DELETE -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/executor/$uuid" | $CURL_FILTER
}


function sechub_executor_update {
  local uuid
  uuid="$(get_executor_uuid $1)"
  is_uuid "$uuid" && curl_with_sechub_auth -i -X PUT -H 'Content-Type: application/json' -d "@$2" "$SECHUB_SERVER/api/admin/config/executor/$uuid" | $CURL_FILTER
}


function sechub_executor_list {
  if [ "$JQ_INSTALLED" == "true" ] ; then
    curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/executors" | $RESULT_FILTER | jq '.executorConfigurations | sort_by(.name)'
  else
    curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/executors" | $RESULT_FILTER
  fi
}


function sechub_job_cancel {
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/jobs/cancel/$1" | $CURL_FILTER
}


function sechub_job_create {
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' --data "@$2" "$SECHUB_SERVER/api/project/$1/job" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_job_approve {
  curl_with_sechub_auth -i -X PUT -H 'Content-Type: application/json' "$SECHUB_SERVER/api/project/$1/job/$2/approve" | $CURL_FILTER
}


function sechub_job_restart {
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/jobs/restart/$1" | $CURL_FILTER
}


function sechub_job_restart_hard {
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/jobs/restart-hard/$1" | $CURL_FILTER
}


function sechub_job_get_report_spdx_json {
  local PROJECT_ID="$1"
  local JOB_UUID="$2"

  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/project/$PROJECT_ID/report/spdx/$JOB_UUID"
}


function sechub_job_list_running {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/jobs/running" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_job_status {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/project/$1/job/$2" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_job_upload_sourcecode {
  local project_id="$1"
  local job_uuid="$2"
  local zip_file="$3"

  if [[ ! -f "$zip_file" ]] ; then
    echo "File \"$zip_file\" does not exist."
    exit 1
  fi

  local checkSum=$(sha256sum "$zip_file" | cut --delimiter=' ' --fields=1)

  curl_with_sechub_auth -i -X POST --header "Content-Type: multipart/form-data" \
    --form "file=@$zip_file" \
    --form "checkSum=$checkSum" \
    "$SECHUB_SERVER/api/project/$project_id/job/$job_uuid/sourcecode" | $CURL_FILTER
}


function sechub_job_upload_binaries {
  local project_id="$1"
  local job_uuid="$2"
  local tar_file="$3"

  if [[ ! -f "$tar_file" ]] ; then
    echo "File \"$tar_file\" does not exist."
    exit 1
  fi

  local checkSum=$(sha256sum "$tar_file" | cut --delimiter=' ' --fields=1)
  local fileSize=$(ls -l "$tar_file" | cut --delimiter=' ' --fields 5)

  curl_with_sechub_auth -i -X POST \
    --header "Content-Type: multipart/form-data" \
    --header "x-file-size: $fileSize" \
    --form "file=@$tar_file" \
    --form "checkSum=$checkSum" \
    "$SECHUB_SERVER/api/project/$project_id/job/$job_uuid/binaries" | $CURL_FILTER
}


function generate_sechub_profile_data {
  local failed=false
  local executor_uuids=""
  # Resolve executor names to their uuids
  while read executor; do
    local uuid=$(get_executor_uuid $executor)
    if is_uuid "$uuid" ; then
      if [ -z "$executor_uuids" ] ; then
        executor_uuids="$uuid"
      else
        executor_uuids="$executor_uuids,$uuid"
      fi
    else
      failed=true
    fi
  done < <(echo "$1" | awk -F',' '{ for( i=1; i<=NF; i++ ) print $i }')
  if $failed ; then
    echo "Error: Could not resolve all executors. See above messages." >&2
    echo "ERROR"
    exit 1
  fi

  local EXECUTORS=$(echo $executor_uuids | awk -F',' '{ for (i = 1; i < NF; i++) { printf("{\"uuid\": \"%s\"}, ", $i) } printf ("{\"uuid\": \"%s\"}", $NF) }')
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
  if [ "$JSON_DATA" = "ERROR" ] ; then
    exit 1
  fi
  echo $JSON_DATA | $JSON_FORMATTER
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/admin/config/execution/profile/$1" | $CURL_FILTER
}


function sechub_profile_delete {
  echo "Execution profile \"$1\" will be deleted. This cannot be undone."
  are_you_sure
  curl_with_sechub_auth -i -X DELETE -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/execution/profile/$1" | $CURL_FILTER
}


function sechub_profile_details {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/execution/profile/$1" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_profile_list {
  if [ "$JQ_INSTALLED" == "true" ] ; then
    curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/execution/profiles" | $RESULT_FILTER | jq '.executionProfiles | sort_by(.id)'
  else
    curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/execution/profiles" | $RESULT_FILTER
  fi
}


function sechub_profile_update {
  local JSON_DATA="$(generate_sechub_profile_data $2 $3)"
  if [ "$JSON_DATA" = "ERROR" ] ; then
    exit 1
  fi
  echo $JSON_DATA | $JSON_FORMATTER
  curl_with_sechub_auth -i -X PUT -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/admin/config/execution/profile/$1" | $CURL_FILTER
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
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/execution/profile/$2/project/$1" | $CURL_FILTER
}


function sechub_project_assign_template {
  curl_with_sechub_auth -i -X PUT -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1/template/$2" | $CURL_FILTER
}


function sechub_project_assign_user {
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1/membership/$2" | $CURL_FILTER
}


function generate_sechub_project_create_data {
  local PROJECT_ID="$(tr [A-Z] [a-z] <<< "$1")"  # Convert to lowercase
  shift
  local OWNER="$(tr [A-Z] [a-z] <<< "$1")"
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
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/admin/project" | $CURL_FILTER
}


function sechub_project_delete {
  echo "Project \"$1\" will be deleted. This cannot be undone."
  are_you_sure
  curl_with_sechub_auth -i -X DELETE -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1" | $CURL_FILTER
}


function sechub_project_details {
  local project_not_found_pattern="\"status\":404"
  local result=$(curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1" | $RESULT_FILTER)
  echo "$result" | $JSON_FORMATTER

  if echo $result | grep "$project_not_found_pattern" >/dev/null 2>&1 ; then
    # Exit with non-zero because project does not exist
    exit 1
  fi
}


function sechub_project_details_all {
  local project_id="$1"
  sechub_project_details $project_id

  echo "Assigned profiles:"
  sechub_profile_list | jq '.[].id' | sed 's/"//g' | while read profile_id ; do
    sechub_profile_details $profile_id | jq '.projectIds' | tr --delete ' ",' | grep --line-regexp $project_id >/dev/null 2>&1
    if [ "$?" = "0" ] ; then
      # Print details about profile
      profile_short_description $profile_id
    fi
  done
}


function sechub_project_falsepositives_list {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/project/$1/false-positives" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_project_joblist {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/project/$PROJECT_ID/jobs?size=$JOBLIST_SIZE&page=$JOBLIST_PAGE" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_project_list {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/projects" | $RESULT_FILTER | $JSON_FORMAT_SORT
}


function sechub_project_metadata_set {
  local key
  local value
  local PROJECT_ID="$(tr [A-Z] [a-z] <<< "$1")"  # Convert to lowercase
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
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/admin/project/$PROJECT_ID/metadata" | $CURL_FILTER
}


function sechub_project_mockdata_list {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/project/$1/mockdata" | $RESULT_FILTER | $JSON_FORMATTER
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
  curl_with_sechub_auth -i -X PUT -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/project/$1/mockdata" | $CURL_FILTER
}


function sechub_project_scan_list {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1/scan/logs" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_project_set_accesslevel {
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1/accesslevel/$2" | $CURL_FILTER
}


function sechub_project_set_owner {
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1/owner/$2" | $CURL_FILTER
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
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/admin/project/$1/whitelist" | $CURL_FILTER
}


function sechub_project_unassign_profile {
  curl_with_sechub_auth -i -X DELETE -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/execution/profile/$2/project/$1" | $CURL_FILTER
}


function sechub_project_unassign_template {
  curl_with_sechub_auth -i -X DELETE -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1/template/$2" | $CURL_FILTER
}


function sechub_project_unassign_user {
  curl_with_sechub_auth -i -X DELETE -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1/membership/$2" | $CURL_FILTER
}


function sechub_scheduler_disable {
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/scheduler/disable/job-processing" > /dev/null 2>&1
  sechub_scheduler_status
}


function sechub_scheduler_enable {
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/scheduler/enable/job-processing" > /dev/null 2>&1
  sechub_scheduler_status
}


function sechub_scheduler_status {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/status" | $RESULT_FILTER | $JSON_FORMAT_SORT | jq '.[0]'
}


function generate_server_encryption_data {
  cat <<EOF
{
  "algorithm" : "$1",
  "passwordSourceType" : "$2",
  "passwordSourceData" : "$3"
}
EOF
}

function sechub_server_encryption_rotate {
  local algorithm="$1"
  local encryption="$2"
  if [[ ! "$encryption" =~ ^var=.+ ]] ; then
    echo "Error: Unknown encryption key definition. Expected 'var=<env-var>' format"
    exit 1
  fi
  local var_name=`echo "$encryption" | cut -d = -f 2`
  
  local JSON_DATA=$(generate_server_encryption_data "$algorithm" "ENVIRONMENT_VARIABLE" "$var_name")
  echo $JSON_DATA | $JSON_FORMATTER
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/admin/encryption/rotate" | $CURL_FILTER
}


function sechub_server_encryption_status {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/encryption/status" | $RESULT_FILTER | jq '.domains'
}


function sechub_server_info {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/info/server" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_server_status {
  # 1. Update status in admin domain
  curl_with_sechub_auth -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/scheduler/status/refresh" > /dev/null 2>&1
  # 2. Get status
  local result_json
  result_json=$(curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/status" | $RESULT_FILTER)
  # 3. Display result
  if [ -n "$TABLE_FORMATTER" -a "$TABLE_FORMATTER" != "$NOFORMAT_PIPE" -a "$JQ_INSTALLED" == "true" ] ; then
    # Print as table to save space
    local result_data
    result_data=( $(printf "key|value\n---|-----\n" && echo $result_json | jq -r '.[] | .key + "|" + .value' | sort) )
    printf "%s\n" ${result_data[@]} | $TABLE_FORMATTER
  else
    # Fallback: Print raw JSON
    echo $result_json | $JSON_FORMATTER
  fi
}


function sechub_server_version {
  local result_json
  result_json=$(curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/info/server" | $RESULT_FILTER)

  if [ "$JSON_FORMATTER" != "$NOFORMAT_PIPE" -a "$JQ_INSTALLED" == "true" ] ; then
    echo $result_json | jq --raw-output '.serverVersion'
  else
    # Fallback: Print raw JSON
    echo -n $result_json | $JSON_FORMATTER
  fi
}


function sechub_superadmin_grant {
  curl_with_sechub_auth -i -X POST "$SECHUB_SERVER/api/admin/user/$1/grant/superadmin" | $CURL_FILTER
}


function sechub_superadmin_list {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/admins" | $RESULT_FILTER | $JSON_FORMAT_SORT
}


function sechub_superadmin_revoke {
  curl_with_sechub_auth -i -X POST "$SECHUB_SERVER/api/admin/user/$1/revoke/superadmin" | $CURL_FILTER
}


function sechub_template_create {
  curl_with_sechub_auth -i -X PUT -H 'Content-Type: application/json' -d "@$2" "$SECHUB_SERVER/api/admin/template/$1" | $CURL_FILTER
}


function sechub_template_delete {
  echo "Template \"$1\" will be deleted. This cannot be undone."
  are_you_sure
  curl_with_sechub_auth -i -X DELETE "$SECHUB_SERVER/api/admin/template/$1" | $CURL_FILTER
}


function sechub_template_details {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/template/$1" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_template_list {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/templates" | $RESULT_FILTER | $JSON_FORMAT_SORT
}


function sechub_user_change_email {
  curl_with_sechub_auth -i -X PUT "$SECHUB_SERVER/api/admin/user/$1/email/$2" | $CURL_FILTER
}


function sechub_user_delete {
  echo "User \"$1\" will be deleted. This cannot be undone."
  are_you_sure
  curl_with_sechub_auth -i -X DELETE "$SECHUB_SERVER/api/admin/user/$1" | $CURL_FILTER
}


function sechub_user_details {
  local sechub_user_or_email="$1"
  if [[ $sechub_user_or_email =~ ^.+@.+$ ]]; then
    curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/user-by-email/$sechub_user_or_email" | $RESULT_FILTER | $JSON_FORMATTER
  else
    curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/user/$sechub_user_or_email" | $RESULT_FILTER | $JSON_FORMATTER
  fi
}


function sechub_user_list {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/users" | $RESULT_FILTER | $JSON_FORMAT_SORT
}


function sechub_user_list_open_signups {
  curl_with_sechub_auth -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/signups" | $RESULT_FILTER | $JSON_FORMAT_SORT
}


function sechub_user_reset_apitoken {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/anonymous/refresh/apitoken/$1" | $CURL_FILTER
}


function generate_sechub_user_signup_data {
  cat <<EOF
{
  "apiVersion":"$SECHUB_API_VERSION",
  "userId":"$1",
  "emailAddress":"$2"
}
EOF
}


function sechub_user_signup {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' \
    -d "$(generate_sechub_user_signup_data $1 $2)" \
    "$SECHUB_SERVER/api/anonymous/signup" | $CURL_FILTER
}


function sechub_user_signup_accept {
  curl_with_sechub_auth -i -X POST "$SECHUB_SERVER/api/admin/signup/accept/$1" | $CURL_FILTER
}


function sechub_user_signup_decline {
  curl_with_sechub_auth -i -X DELETE "$SECHUB_SERVER/api/admin/signup/$1" | $CURL_FILTER
}


# -----------------------------
# main
MANDATORY_EXECUTABLES="awk curl cut sha256sum"   # space separated list
# Check prepreqs
for i in $MANDATORY_EXECUTABLES ; do
    check_executable_is_installed $i
done

failed=false
YES=false
SECHUB_API_VERSION="1.0"
NOFORMAT_PIPE="cat -"
RESULT_FILTER="tail -1"
TIMESTAMP=`date +"%Y-%m-%d %H:%M %Z"`
if which jq >/dev/null 2>&1 ; then
  JQ_INSTALLED="true"
  JSON_FORMATTER="jq ."   # . is needed or pipeing the result is not possible
  JSON_FORMAT_SORT="jq sort"
else
  echo "### Hint: Install jq (https://github.com/stedolan/jq). Now executor access by name will not work." >&2  # appears only on stderr
  JQ_INSTALLED="false"
  JSON_FORMATTER="$NOFORMAT_PIPE"
  JSON_FORMAT_SORT="$NOFORMAT_PIPE"
fi
if which column >/dev/null 2>&1 ; then
  TABLE_FORMATTER="column -t -s '|'"
else
  echo "### Hint: Install column to improve table output." >&2  # appears only on stderr
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
    TABLE_FORMATTER="$NOFORMAT_PIPE"
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
CURL_PARAMS="--silent --insecure --show-error"
CURL_FILTER="grep -i http/\|error"

action="$1" && shift
case "$action" in
  alive_check)
    failed=false
    check_parameter "SECHUB_SERVER" "\$SECHUB_SERVER"
    $failed || sechub_alive_check
    ;;
  asset_file_create)
    ASSET_ID="$1" ; check_parameter ASSET_ID '<asset id>'
    ASSET_FILE="$2" ; check_file "$ASSET_FILE" '<file>'
    $failed || sechub_asset_file_create "$ASSET_ID" "$ASSET_FILE"
    ;;
  asset_file_delete)
    ASSET_ID="$1" ; check_parameter ASSET_ID '<asset id>'
    ASSET_FILE="$2" ; check_parameter ASSET_FILE '<file>'
    $failed || sechub_asset_file_delete "$ASSET_ID" "$ASSET_FILE"
    ;;
  asset_file_download)
    ASSET_ID="$1" ; check_parameter ASSET_ID '<asset id>'
    ASSET_FILE="$2" ; check_parameter ASSET_FILE '<file>'
    $failed || sechub_asset_file_download "$ASSET_ID" "$ASSET_FILE"
    ;;
  asset_delete)
    ASSET_ID="$1" ; check_parameter ASSET_ID '<asset id>'
    $failed || sechub_asset_delete "$ASSET_ID"
    ;;
  asset_details)
    ASSET_ID="$1" ; check_parameter ASSET_ID '<asset id>'
    $failed || sechub_asset_details "$ASSET_ID"
    ;;
  asset_list)
    $failed || sechub_asset_list
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
    EXECUTOR="$1" ; check_parameter EXECUTOR '<executor uuid or name>'
    $failed || sechub_executor_delete "$EXECUTOR"
    ;;
  executor_details)
    EXECUTOR="$1" ; check_parameter EXECUTOR '<executor uuid or name>'
    $failed || sechub_executor_details "$EXECUTOR"
    ;;
  executor_list)
    $failed || sechub_executor_list
    ;;
  executor_update)
    EXECUTOR="$1" ; check_parameter EXECUTOR '<executor uuid or name>'
    EXECUTOR_JSONFILE="$2" ; check_file "$EXECUTOR_JSONFILE" '<json-file>'
    $failed || sechub_executor_update "$EXECUTOR" "$EXECUTOR_JSONFILE"
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
  project_assign_template)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    TEMPLATE_ID="$2" ; check_parameter TEMPLATE_ID '<template-id>'
    $failed || sechub_project_assign_template "$PROJECT_ID" "$TEMPLATE_ID"
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
  project_joblist)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    JOBLIST_SIZE="$2"   ; check_parameter JOBLIST_SIZE '<size>'
    JOBLIST_PAGE="$3"   ; check_parameter JOBLIST_PAGE '<page>'
    $failed || sechub_project_joblist
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
  project_unassign_template)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID '<project-id>'
    TEMPLATE_ID="$2" ; check_parameter TEMPLATE_ID '<template-id>'
    $failed || sechub_project_unassign_template "$PROJECT_ID" "$TEMPLATE_ID"
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
  server_encryption_rotate)
    ALOGORITHM="$1" ; check_parameter ALOGORITHM '<algorithm>'
    ENCRYPTION_KEY="$2" ; check_parameter ENCRYPTION_KEY 'var=<env-var>'
    $failed || sechub_server_encryption_rotate "$ALOGORITHM" "$ENCRYPTION_KEY"
    ;;
  server_encryption_status)
    $failed || sechub_server_encryption_status
    ;;
  server_info)
    $failed || sechub_server_info
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
  template_create)
    TEMPLATE_ID="$1" ; check_parameter TEMPLATE_ID '<template id>'
    TEMPLATE_JSONFILE="$2" ; check_file "$TEMPLATE_JSONFILE" '<json-file>'
    $failed || sechub_template_create "$TEMPLATE_ID" "$TEMPLATE_JSONFILE"
    ;;
  template_delete)
    TEMPLATE_ID="$1" ; check_parameter TEMPLATE_ID '<template id>'
    $failed || sechub_template_delete "$TEMPLATE_ID"
    ;;
  template_details)
    TEMPLATE_ID="$1" ; check_parameter TEMPLATE_ID '<template id>'
    $failed || sechub_template_details "$TEMPLATE_ID"
    ;;
  template_list)
    $failed || sechub_template_list
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
    SECHUB_USER_OR_EMAIL="$1" ; check_parameter SECHUB_USER_OR_EMAIL '<user-id or email>'
    $failed || sechub_user_details "$SECHUB_USER_OR_EMAIL"
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
