#!/bin/bash
# SPDX-License-Identifier: MIT

file_to_upload="$1"
json_config="$2"
upload_type="source"
job_start_datetime=""

function usage() {

    cat - <<USAGE
usage: `basename $0` <file-to-upload> [<job-configuration-file>]

# Please set the environment variables:

export PDS_SERVER=https://<server>:<port>
export PDS_USERID=<username>
export PDS_APITOKEN=<password>
export PDS_PRODUCT_IDENTFIER=<pds-product-identifier>

optional:

export RETRIES=<number-of-retries>
export RESULT_FILE=<path-to-result-file>


# Example:

export PDS_SERVER=https://localhost:8444
export PDS_USERID=admin
export PDS_APITOKEN="pds-apitoken"
export PDS_PRODUCT_IDENTFIER=PDS_GOSEC
USAGE
}

function did_job_fail() {
    local status="$1"
    job_failed="no"

    if [[ $status == "FAILED" || $status == "CANCELED" ]]
    then
        job_failed="yes"
    fi

    echo $job_failed
}

function is_job_finished() {
    local status="$1"
    job_finished="no"

    if [[ $status == "DONE" || $status == "FINISHED" || $status == "FAILED" || $status == "CANCELED" ]]
    then
        job_finished="yes"
    fi

    echo $job_finished
}

function is_job_running() {
    local status="$1"
    job_running="no"

    if [[ $status == "RUNNING" ]]
    then
        job_running="yes"
    fi

    echo $job_running
}

function parameter_missing() {
    MESSAGE="$1"

    printf "[ERROR] $MESSAGE\n"

    usage

    exit 1
}

function seconds_in_between() {
    local start_datetime="$1"
    local end_datetime="$2"

    start_time=$(date --date "$start_datetime" '+%s')
    end_time=$(date --date "$end_datetime" '+%s')

    seconds=$(( end_time - start_time ))

    echo "$seconds"
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

if [[ -z "$PDS_PRODUCT_IDENTFIER" ]]
then
    parameter_missing "Environment variable PDS_PRODUCT_IDENTFIER missing."
fi

if [[ ! -z "$RETRIES" ]]
then
    retries=$RETRIES
else
    retries=1200
fi

echo "Aborting after a maximum of $retries retries"

if [[ -z "$file_to_upload" ]]
then
    parameter_missing "Please provide a file to upload."
fi

if [[ ! -f "$file_to_upload" ]]
then
    parameter_missing "File $file_to_upload does not exist."
fi

if [[ ! -z "$json_config" ]]
then
    echo "JSON job configuration provided."

    if [[ ! -f "$json_config" ]]
    then
        echo "File $json_config does not exist."
        exit 1
    fi
fi

cd $(dirname "$0")
pds_api="../../sechub-developertools/scripts/pds-api.sh"

check_alive=`$pds_api check_alive`

if [[ -z $check_alive ]]
then
    printf "\n[ERROR] The PDS server $PDS_SERVER is not to running.\n"
    printf "[ERROR] Check if the PDS is running.\n"
    exit 3
fi

sechub_job_uuid=`uuidgen`

if [[ ! -z "$json_config" ]]
then
    jobUUID=`$pds_api create_job_from_json "$json_config" | jq '.jobUUID' | tr -d \"`
else
    jobUUID=`$pds_api create_job "$PDS_PRODUCT_IDENTFIER" "$sechub_job_uuid" | jq '.jobUUID' | tr -d \"`
fi

echo "Job created. Job UUID: $jobUUID"

echo "Uploading file: $file_to_upload"
"$pds_api" upload "$jobUUID" "$file_to_upload"

"$pds_api" mark_job_ready_to_start "$jobUUID"
echo "Job $jobUUID marked as ready to start."

# Check the status of the job
status=""

while [[ $retries -ge 0 && $(is_job_finished $status) == "no" ]]
do
    status=`$pds_api job_status "$jobUUID" | jq '.state' | tr -d \"`

    if [[ -z "$job_start_datetime" &&  $(is_job_running $status) == "yes" ]]
    then
        job_start_datetime=$( date +"%Y-%m-%d %T" )
        echo "Job started: $job_start_datetime"
    fi

    echo "Job status: $status"

    if (( $retries % 10 == 0 ))
    then
        printf "\n# Job output stream\n"
        "$pds_api" job_stream_output "$jobUUID"
    fi

    ((retries--))
    sleep 0.5s
done

job_end_datetime=$( date +"%Y-%m-%d %T" )
seconds_in_between_start_end=$(seconds_in_between "$job_start_datetime" "$job_end_datetime")

echo "###############"
echo "## Job finished"
echo "###############"
echo ""
echo "Job status: $status"
echo "Job started: $job_start_datetime"
echo "Job ended: $job_end_datetime"
echo "Job duration: ${seconds_in_between_start_end}s"

if [[ $(did_job_fail $status) == "yes" ]]
then
    printf "\n# Job messages\n"
    "$pds_api" job_messages "$jobUUID"

    printf "\n# Job output stream\n"
    "$pds_api" job_stream_output "$jobUUID"

    printf "\n# Job error stream\n"
    "$pds_api" job_stream_error "$jobUUID"

    exit 1
else
    printf "\n# Job messages\n"
    "$pds_api" job_messages "$jobUUID"

    printf "\n# Job output stream\n"
    "$pds_api" job_stream_output "$jobUUID"

    if [[ -n "$RESULT_FILE" ]]
    then
        printf "\n# Writing result to file $RESULT_FILE\n"
        "$pds_api" job_result "$jobUUID" > "$RESULT_FILE"
    else
        printf "\n# Return the result\n"
        "$pds_api" job_result "$jobUUID"
    fi
fi
