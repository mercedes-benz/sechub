#!/usr/bin/env bash

file_to_upload="$1"
retries=20

function usage() {
    local script_name=
    echo "`basename $0` <file-to-upload>"
    echo ""
    
    cat <<'USAGE'
Please set the environment variables:

export PDS_SERVER=https://<server>:<port>
export PDS_USERID=<username>
export PDS_APITOKEN=<password>

Example:

export PDS_SERVER=https://localhost:8444
export PDS_USERID=admin
export PDS_APITOKEN="pds-apitoken"
USAGE
}

function is_job_finished () {
    local status="$1"
    job_finished="no"

    if [[ $status == "DONE" || $status == "FINISHED" ]]
    then
        job_finished="yes"
    fi

    echo $job_finished
}

function parameter_missing() {
    MESSAGE="$1"

    printf "[ERROR] $MESSAGE\n"
    usage
    exit 1
}

if [[ -z "$PDS_SERVER" ]]
then
    parameter_missing "Environment variable PDS_SERVER missing."
fi

if [[ -z "$PDS_USERID" ]]
then
    parameter_missing "Environment variable PDS_USERID missing."
fi

if [[ -z "$PDS_APITOKEN" ]]
then
    parameter_missing "Environment variable PDS_APITOKEN missing."
fi

if [[ -z "$file_to_upload" ]]
then
    parameter_missing "Please provide a file to upload."
fi

if [[ ! -f "$file_to_upload" ]]
then
    parameter_missing "File $file_to_upload does not exist."
fi

pds_api="../../sechub-developertools/scripts/pds-api.sh"

check_alive=`$pds_api check_alive`

if [[ -z $check_alive ]]
then
    printf "\n[ERROR] The PDS server $PDS_SERVER is not to running.\n"
    printf "[ERROR] Check if the PDS is running.\n"
    exit 3
fi

sechub_job_uuid=`uuidgen`
jobUUID=`$pds_api create_job PDS_GOSEC "$sechub_job_uuid" | jq '.jobUUID' | tr -d \"`

echo "Job created. Job UUID: $jobUUID."

"$pds_api" upload_zip "$jobUUID" "$file_to_upload"

"$pds_api" mark_job_ready_to_start "$jobUUID"

echo "Job $jobUUID marked as ready to start."

# Check the status of the job
status=""

while [[ $retries -ge 0 && $(is_job_finished $status) == "no" ]]
do
    status=`$pds_api job_status "$jobUUID" | jq '.state' | tr -d \"`
    echo "Job status: $status"
    ((retries--))
    sleep 0.5
done

# echo return the actual result
"$pds_api" job_result "$jobUUID"