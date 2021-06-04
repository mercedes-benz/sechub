#!/bin/bash
# SPDX-License-Identifier: MIT

# ---------------------------------------------
# Shell front end to selected SecHub API calls
# ---------------------------------------------
# This can be handy for batch operations (e.g. creation or modification of multiple objects)
# or for using shell tools like grep to search the results
#
# SecHub Rest API documentation: https://daimler.github.io/sechub/latest/sechub-restapi.html

# Tip: Set SECHUB_SERVER, SECHUB_USERID and SECHUB_APITOKEN as environment variables

function usage {
  cat - <<EOF
---
Usage: `basename $0` [-p] [-s <sechub server url> [-u <sechub user>] [-a <sechub api token>] action [<action's parameters>]

Shell front end to selected SecHub API calls.
Output will be beautified/colorized by piping json output through jq command (https://github.com/stedolan/jq)
unless you specify -p or -plain option.

You are encouraged to set SECHUB_SERVER, SECHUB_USERID and SECHUB_APITOKEN as environment variables
so you can omit setting them via options which is better, because your secrets will not be revealed in the process list.

List of actions and mandatory parameters:
ACTION [PARAMETERS] - EXPLANATION
----------------------------------
executor_details <executor-uuid> - Show definition of executor <executor-uuid>
executor_list - List all existing executors (json format)
job_cancel <job-uuid> - Cancel job <job-uuid>
job_list_running - List running jobs (json format)
job_restart <job-uuid> - Restart/activate job <job-uuid>
job_restart_hard <job-uuid> - Run new backend scan of job <job-uuid>
job_status <project-id> <job-uuid> - Get status of job <job-uuid> in project <project-id> (json format)
profile_details <profile-id> - Show details of execution profile <profile-id> (e.g. assinged executors)
profile_list - List all existing execution profiles (json format)
project_assign_profile <project-id> <profile-id> - Assign execution profile <profile-id> to project <project-id>
project_assign_user <project-id> <user-id> - Assign user to project (allow scanning)
project_create <project-id> <owner> ["<project short description>"] - Create a new project. The short description is optional.
project_details <project-id> - Show owner, users, whitelist etc. of project <project-id>
project_details_all <project-id> - project_details plus assigned execution profiles
project_falsepositives_list <project-id> - Get defined false-positives for project <project-id> (json format)
project_list - List all projects (json format)
project_scan_list <project-id> - List scan jobs for project <project-id> (json format)
project_unassign_profile <project-id> <profile-id> - Unassign execution profile <profile-id> from project <project-id>
project_unassign_user <project-id> <user-id> - Unassign user from project (revoke scanning)
scheduler_disable - Stop SecHub job scheduler
scheduler_enable - Continue SecHub job scheduler
scheduler_status - Get scheduler status
server_status - Get status entries of SecHub server like scheduler, jobs etc. (json format)
server_version - Print version of SecHub server
user_details <user-id> - List details of user <user-id> (json format)
user_list - List all users (json format)
user_list_admins - List all superadmin users (json format)
user_list_open_signups - List all users waiting to get their signup accepted (json format)
user_reset_apitoken <email address> - Request new api token for <email address> (invalidates current api token immediately)
user_signup <new user-id> <email address> - Add <new user-id> with <email address> (needs to be accepted then)
user_signup_accept <new user-id> - Accept registration of <new user-id>
user_signup_decline <new user-id> - Decline registration of <new user-id>

EOF
}


function check_parameter {
  param="$1"
  if [ -z "${!param}" ] ; then
    echo "$param not set"
    failed=1
  fi
}


function sechub_executor_details {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/executor/$1" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_executor_list {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/executors" | $RESULT_FILTER | jq '.executorConfigurations'
}


function sechub_job_cancel {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/jobs/cancel/$1"
}


function sechub_job_restart {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/jobs/restart/$1"
}


function sechub_job_restart_hard {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/jobs/restart-hard/$1"
}


function sechub_job_list_running {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/jobs/running" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_job_status {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/project/$1/job/$2" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_profile_details {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/execution/profile/$1" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_profile_list {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/config/execution/profiles" | $RESULT_FILTER | jq '.executionProfiles'
}


function profile_short_description {
  profile_id="$1"
  mapfile -t resultArray < <(sechub_profile_details $profile_id | jq '.enabled,.configurations[].name')
  enabled=${resultArray[0]}
  if [ "$enabled" = "true" ] ; then
    enabledState="enabled"
  else
    enabledState="disabled"
  fi
  first_run="true"
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
  JSON_DATA="$(generate_sechub_project_create_data $1 $2 $3)"
  echo $JSON_DATA  # Show what is sent
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' -d "$JSON_DATA" "$SECHUB_SERVER/api/admin/project"
}


function sechub_project_details {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_project_details_all {
  project_id="$1"
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


function sechub_project_scan_list {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/project/$1/scan/logs" | $RESULT_FILTER | $JSON_FORMATTER
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
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/status" | $RESULT_FILTER | $JSON_FORMAT_SORT
}


function sechub_server_version {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: text/plain' "$SECHUB_SERVER/api/admin/info/version" | $RESULT_FILTER
}


function sechub_user_details {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/user/$1" | $RESULT_FILTER | $JSON_FORMATTER
}


function sechub_user_list {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/users" | $RESULT_FILTER | $JSON_FORMAT_SORT
}


function sechub_user_list_admins {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/admins" | $RESULT_FILTER | $JSON_FORMAT_SORT
}


function sechub_user_list_open_signups {
  curl $CURL_PARAMS -i -X GET -H 'Content-Type: application/json' "$SECHUB_SERVER/api/admin/signups" | $RESULT_FILTER | $JSON_FORMAT_SORT
}


function sechub_user_reset_apitoken {
  curl $CURL_PARAMS -i -X POST -H 'Content-Type: application/json' "$SECHUB_SERVER/api/anonymous/refresh/apitoken/$1"
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
    "$SECHUB_SERVER/api/anonymous/signup"
}


function sechub_user_signup_accept {
  curl $CURL_PARAMS -i -X POST "$SECHUB_SERVER/api/admin/signup/accept/$1"
}


function sechub_user_signup_decline {
  curl $CURL_PARAMS -i -X DELETE "$SECHUB_SERVER/api/admin/signup/$1"
}

# -----------------------------
# main
failed=0
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
  check_parameter "$i"
done
AUTH="$SECHUB_USERID:$SECHUB_APITOKEN"
CURL_PARAMS="-u $AUTH --silent --insecure --show-error"

action="$1" && shift
case "$action" in
  executor_details)
    EXECUTOR_UUID="$1" ; check_parameter EXECUTOR_UUID
    [ $failed == 0 ] && sechub_executor_details "$EXECUTOR_UUID"
    ;;
  executor_list)
    [ $failed == 0 ] && sechub_executor_list
    ;;
  job_cancel)
    JOB_UUID="$1" ; check_parameter JOB_UUID
    [ $failed == 0 ] && sechub_job_cancel "$JOB_UUID"
    ;;
  job_list_running)
    [ $failed == 0 ] && sechub_job_list_running
    ;;
  job_restart)
    JOB_UUID="$1" ; check_parameter JOB_UUID
    [ $failed == 0 ] && sechub_job_restart "$JOB_UUID"
    ;;
  job_restart_hard)
    JOB_UUID="$1" ; check_parameter JOB_UUID
    [ $failed == 0 ] && sechub_job_restart_hard "$JOB_UUID"
    ;;
  job_status)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID
    JOB_UUID="$2"   ; check_parameter JOB_UUID
    [ $failed == 0 ] && sechub_job_status "$PROJECT_ID" "$JOB_UUID"
    ;;
  profile_details)
    PROFILE_ID="$1" ; check_parameter PROFILE_ID
    [ $failed == 0 ] && sechub_profile_details "$PROFILE_ID"
    ;;
  profile_list)
    [ $failed == 0 ] && sechub_profile_list
    ;;
  project_assign_profile)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID
    PROFILE_ID="$2" ; check_parameter PROFILE_ID
    [ $failed == 0 ] && sechub_project_assign_profile "$PROJECT_ID" "$PROFILE_ID"
    ;;
  project_assign_user)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID
    SECHUB_USER="$2" ; check_parameter SECHUB_USER
    [ $failed == 0 ] && sechub_project_assign_user "$PROJECT_ID" "$SECHUB_USER"
    ;;
  project_create)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID
    PROJECT_OWNER="$2" ; check_parameter PROJECT_OWNER
    shift
    shift
    if [ $# -gt 0 ] ; then
      PROJECT_SHORT_DESCRIPTION="$*"
    else
      PROJECT_SHORT_DESCRIPTION="Created by sechub-api.sh at $TIMESTAMP"
    fi
    [ $failed == 0 ] && sechub_project_create "$PROJECT_ID" "$PROJECT_OWNER" "$PROJECT_SHORT_DESCRIPTION"
    ;;
  project_details)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID
    [ $failed == 0 ] && sechub_project_details "$PROJECT_ID"
    ;;
  project_details_all)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID
    [ $failed == 0 ] && sechub_project_details_all "$PROJECT_ID"
    ;;
  project_falsepositives_list)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID
    [ $failed == 0 ] && sechub_project_falsepositives_list "$PROJECT_ID"
    ;;
  project_list)
    [ $failed == 0 ] && sechub_project_list
    ;;
  project_scan_list)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID
    [ $failed == 0 ] && sechub_project_scan_list "$PROJECT_ID"
    ;;
  project_unassign_profile)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID
    PROFILE_ID="$2" ; check_parameter PROFILE_ID
    [ $failed == 0 ] && sechub_project_unassign_profile "$PROJECT_ID" "$PROFILE_ID"
    ;;
  project_unassign_user)
    PROJECT_ID="$1" ; check_parameter PROJECT_ID
    SECHUB_USER="$2" ; check_parameter SECHUB_USER
    [ $failed == 0 ] && sechub_project_unassign_user "$PROJECT_ID" "$SECHUB_USER"
    ;;
  scheduler_disable)
    [ $failed == 0 ] && sechub_scheduler_disable
    ;;
  scheduler_enable)
    [ $failed == 0 ] && sechub_scheduler_enable
    ;;
  scheduler_status)
    [ $failed == 0 ] && sechub_scheduler_status
    ;;
  server_status)
    [ $failed == 0 ] && sechub_server_status
    ;;
  server_version)
    [ $failed == 0 ] && sechub_server_version
    ;;
  user_details)
    SECHUB_USER="$1" ; check_parameter SECHUB_USER
    [ $failed == 0 ] && sechub_user_details "$SECHUB_USER"
    ;;
  user_list)
    [ $failed == 0 ] && sechub_user_list
    ;;
  user_list_admins)
    [ $failed == 0 ] && sechub_user_list_admins
    ;;
  user_list_open_signups)
    [ $failed == 0 ] && sechub_user_list_open_signups
    ;;
  user_reset_apitoken)
    SECHUB_EMAIL="$1" ; check_parameter SECHUB_EMAIL
    [ $failed == 0 ] && sechub_user_reset_apitoken "$SECHUB_EMAIL"
    ;;
  user_signup)
    SECHUB_USER="$1" ; check_parameter SECHUB_USER
    SECHUB_EMAIL="$2" ; check_parameter SECHUB_EMAIL
    [ $failed == 0 ] && sechub_user_signup "$SECHUB_USER" "$SECHUB_EMAIL"
    ;;
  user_signup_decline)
    SECHUB_USER="$1" ; check_parameter SECHUB_USER
    [ $failed == 0 ] && sechub_user_signup_decline "$SECHUB_USER"
    ;;
  user_signup_accept)
    SECHUB_USER="$1" ; check_parameter SECHUB_USER
    [ $failed == 0 ] && sechub_user_signup_accept "$SECHUB_USER"
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
if [ $failed != 0 ] ; then
  usage
  exit 1
fi
