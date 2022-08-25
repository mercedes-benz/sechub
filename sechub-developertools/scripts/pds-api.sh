#!/bin/bash
# SPDX-License-Identifier: MIT

# ---------------------------------------------
# Shell front end to selected SecHub API calls
# ---------------------------------------------

# Tip: Set PDS_SERVER, PDS_USERID and PDS_APITOKEN as environment variables

PDS_API_VERSION="1.0"

function usage {
  cat - <<EOF
---
Usage: `basename $0` [-p] [-s <pds server url> [-u <pds user>] [-a <pds api token>] action [<action's parameters>]

Shell front end for the Product Delegation Server (PDS)
Output will be beautified/colorized by piping json output through jq command (https://github.com/stedolan/jq)
unless you specify -p or -plain option.

You are encouraged to set PDS_SERVER, PDS_USERID and PDS_APITOKEN as environment variables
so you can omit setting them via options which is better, because your secrets will not be revealed in the process list.

List of actions and mandatory parameters:
ACTION [PARAMETERS] - EXPLANATION
----------------------------------
check_alive - Check if the server is running.
create_job <product-id> <sechub-job-uuid> - Create a new job using <product-id> and a <sechub-job-uuid>.
create_job_from_json <json-file> - Create a new job using a <json-file> JSON file.
upload <job-uuid> <file> - Upload a <file> file for an existing job <job-uuid>.
mark_job_ready_to_start <job-uuid> - Mark a job with <job-uuid> as ready to start.
job_status <job-uuid> - Get the status of a job using the <job-uuid>.
job_result <job-uuid> - Get the job result using the <job-uuid>
monitoring_status - Monitoring information about the server and jobs
job_stream_output <job-uuid> - Get the job ouput stream content
job_stream_error <job-uuid> - Get the job error stream content
EOF
}

function check_parameter {
  param="$1"
  if [ -z "${!param}" ] ; then
    echo "$param not set"
    FAILED=1
  fi
}

function check_alive {
  curl $CURL_PARAMS --head "$PDS_SERVER/api/anonymous/check/alive"
}

function mark_job_ready_to_start {
  local jobUUID=$1

  curl $CURL_AUTH $CURL_PARAMS -i -X PUT --header "Accept: application/json" --header "Content-Type: application/json" "$PDS_SERVER/api/job/$jobUUID/mark-ready-to-start" | $RESULT_FILTER | $JSON_FORMATTER
}

function job_status {
  local jobUUID=$1

  curl $CURL_AUTH $CURL_PARAMS -i -X GET --header "Accept: application/json" "$PDS_SERVER/api/job/$jobUUID/status" | $RESULT_FILTER | $JSON_FORMATTER
}

function job_result {
  local jobUUID=$1

  curl $CURL_AUTH $CURL_PARAMS -X GET --header "Accept: application/json" "$PDS_SERVER/api/job/$jobUUID/result"
  echo ""
}

function job_stream_output {
  local jobUUID=$1

  curl $CURL_AUTH $CURL_PARAMS -X GET --header "Accept: text/plain" "$PDS_SERVER/api/admin/job/$jobUUID/stream/output"
  echo ""
}

function job_stream_error {
  local jobUUID=$1

  curl $CURL_AUTH $CURL_PARAMS -X GET --header "Accept: text/plain" "$PDS_SERVER/api/admin/job/$jobUUID/stream/error"
  echo ""
}

function monitoring_status {
  curl $CURL_AUTH $CURL_PARAMS -X GET --header "Accept: application/json" "$PDS_SERVER/api/admin/monitoring/status" | $RESULT_FILTER | $JSON_FORMATTER
}

function create_job {
  local productId=$1
  local sechubJobUUID=$2

  curl $CURL_AUTH $CURL_PARAMS -i -X POST --header "Content-Type: application/json" \
    --data "$(generate_pds_job_data $sechubJobUUID $productId)" \
    "$PDS_SERVER/api/job/create" | $RESULT_FILTER | $JSON_FORMATTER
}

function create_job_from_json {
  local json_file=$1

  curl $CURL_AUTH $CURL_PARAMS -i -X POST --header "Content-Type: application/json" \
    --data "@$json_file" \
    "$PDS_SERVER/api/job/create" | $RESULT_FILTER | $JSON_FORMATTER
}


function generate_pds_job_data {
  local sechub_job_uuid="$1"
  local product_id="$2"

  cat <<EOF
{
  "apiVersion":"$PDS_API_VERSION",
  "sechubJobUUID":"$sechub_job_uuid",
  "productId":"$product_id"
}
EOF
}

function upload {
  local pdsJobUUID=$1
  local file_to_upload=$2
  local upload_file_name="sourcecode.zip"

  if [[ ! -f "$file_to_upload" ]] ; then
    echo "File \"$file_to_upload\" does not exist."
    exit 1
  fi

  local file_to_upload_lowercased=$( echo "$file_to_upload" | tr '[:upper:]' '[:lower:]' )
  if [[ "$file_to_upload_lowercased" == *.tar ]]
  then
    upload_file_name="binaries.tar"
  fi

  local checkSum=$(sha256sum "$file_to_upload" | cut --delimiter=' ' --fields=1)
  local fileSize=$(ls -l "$file_to_upload" | cut --delimiter=' ' --fields 5)

  curl $CURL_AUTH $CURL_PARAMS -i -X POST \
    --header "Content-Type: multipart/form-data" \
    --header "x-file-size: $fileSize" \
    --form "file=@$file_to_upload" \
    --form "checkSum=$checkSum" \
    "$PDS_SERVER/api/job/${pdsJobUUID}/upload/$upload_file_name" | $RESULT_FILTER

  if [[ "$?" == "0" ]] ; then
    echo "Uploaded file: \"$file_to_upload\""
  else
    echo "Upload failed."
  fi
}

########
# main #
########

FAILED=0

PDS_API_VERSION="1.0"
NOFORMAT_PIPE="cat -"
RESULT_FILTER="tail -1"
if which jq >/dev/null 2>&1 ; then
  JSON_FORMATTER="jq ."   # . is needed or piping the result is not possible
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
    PDS_APITOKEN="$2"
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
    PDS_SERVER="$2"
    shift 2
    ;;
  -u|-user)
    PDS_USERID="$2"
    shift 2
    ;;
  -v|-verbose)
    echo "### Connecting as $PDS_USERID to $PDS_SERVER"
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
for parameter in PDS_SERVER PDS_USERID PDS_APITOKEN ; do
  check_parameter "$parameter"
done

AUTH="$PDS_USERID:$PDS_APITOKEN"
CURL_AUTH="-u $AUTH"
CURL_PARAMS="--silent --insecure --show-error"

action="$1" && shift
case "$action" in
  check_alive)
    check_alive
    ;;
  create_job)
    PDS_PRODUCT_ID="$1" ; check_parameter PDS_PRODUCT_ID
    SECHUB_JOB_UUID="$2" ; check_parameter SECHUB_JOB_UUID
    [ $FAILED == 0 ] && create_job "$PDS_PRODUCT_ID" "$SECHUB_JOB_UUID" 
    ;;
  create_job_from_json)
    JSON_FILE="$1" ; check_parameter JSON_FILE
    [ $FAILED == 0 ] && create_job_from_json "$JSON_FILE" 
    ;;
  upload)
    PDS_JOB_UUID="$1" ; check_parameter PDS_JOB_UUID
    FILE_TO_UPLOAD="$2" ; check_parameter FILE_TO_UPLOAD
    [ $FAILED == 0 ] && upload "$PDS_JOB_UUID" "$FILE_TO_UPLOAD" 
    ;;
  mark_job_ready_to_start)
    JOB_UUID="$1"   ; check_parameter JOB_UUID
    [ $FAILED == 0 ] && mark_job_ready_to_start "$JOB_UUID"
    ;;
  job_status)
    JOB_UUID="$1"   ; check_parameter JOB_UUID
    [ $FAILED == 0 ] && job_status "$JOB_UUID"
    ;;
  job_result)
    JOB_UUID="$1"   ; check_parameter JOB_UUID
    [ $FAILED == 0 ] && job_result "$JOB_UUID"
    ;;
  job_stream_output)
    JOB_UUID="$1"   ; check_parameter JOB_UUID
    [ $FAILED == 0 ] && job_stream_output "$JOB_UUID"
    ;;
  job_stream_error)
    JOB_UUID="$1"   ; check_parameter JOB_UUID
    [ $FAILED == 0 ] && job_stream_error "$JOB_UUID"
    ;;
  monitoring_status)
    [ $FAILED == 0 ] && monitoring_status
    ;;
  "")
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
if [ $FAILED != 0 ] ; then
  usage
  exit 1
fi