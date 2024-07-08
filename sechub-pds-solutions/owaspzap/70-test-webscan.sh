#!/usr/bin/bash
# SPDX-License-Identifier: MIT

target_url_or_config="$1"

target_url=""
json_config=""

function usage() {
    local script_name=
    printf "`basename $0` <target-url or path-to-json-config>\n\n"

    cat <<'USAGE'
# Please set the environment variables:

export PDS_SERVER=https://<server>:<port>
export PDS_USERID=<username>
export PDS_APITOKEN=<password>
export PDS_PRODUCT_IDENTFIER=<pds-product-identifier>

optional:

export RETRIES=<number-of-retries>


# Example:

export PDS_SERVER=https://localhost:8444
export PDS_USERID=admin
export PDS_APITOKEN="pds-apitoken"
export PDS_PRODUCT_IDENTFIER=PDS_OWASP_ZAP
USAGE
}

function did_job_fail() {
    local status="$1"
    job_failed="no"

    if [[ $status == "FAILED" ]]
    then
        job_failed="yes"
    fi

    echo $job_failed
}

function is_job_finished () {
    local status="$1"
    job_finished="no"

    if [[ $status == "DONE" || $status == "FINISHED" || $status == "FAILED" ]]
    then
        job_finished="yes"
    fi

    echo $job_finished
}

function parameter_missing() {
    MESSAGE="$1"

    printf "[ERROR] $MESSAGE\n\n"

    usage

    exit 1
}

function create_json() {
    local target_url="$1"
    local sechub_job_uuid=`uuidgen`

json=$(cat <<JSON
{
    "apiVersion" : "1.0",
    "sechubJobUUID": "$sechub_job_uuid",
    "productId": "$PDS_PRODUCT_IDENTFIER",
    "parameters": [
        {
            "key" : "pds.scan.target.url",
            "value" : "$target_url"
        },
        {
            "key" : "pds.scan.target.type",
            "value" : "INTRANET"
        }
     ]
}
JSON
)

    echo "$json"
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
    retries=1000
fi

if [[ ! -z "$target_url_or_config" ]]
then
    if [[ -f "$target_url_or_config" ]]
    then
        echo "JSON job configuration provided."
        json_config="$target_url_or_config"
    else
        echo "Target Url provided."
        target_url="$target_url_or_config"
    fi
else
    parameter_missing "Parameter Target URL or JSON config is missing."
fi

pds_api="../../sechub-developertools/scripts/pds-api.sh"

check_alive=`$pds_api check_alive`

if [[ -z $check_alive ]]
then
    printf "\n[ERROR] The PDS server $PDS_SERVER is not to running.\n"
    printf "[ERROR] Check if the PDS is running.\n"
    exit 3
fi

FILE_NAME="$PWD/temp.json"
if [[ -z "$json_config" ]]
then
    create_json $target_url > $FILE_NAME
    json_config="$FILE_NAME"
fi

echo "JSON config"
echo "$json_config"

jobUUID=`$pds_api create_job_from_json "$json_config" | jq '.jobUUID' | tr -d \"`
rm "$FILE_NAME"

echo "Job created. Job UUID: $jobUUID."

"$pds_api" mark_job_ready_to_start "$jobUUID"

echo "Job $jobUUID marked as ready to start."

# Check the status of the job
status=""

while [[ $retries -ge 0 && $(is_job_finished $status) == "no" ]]
do
    status=`$pds_api job_status "$jobUUID" | jq '.state' | tr -d \"`
    echo "Job status: $status"

    if (( $retries % 10 == 0 ))
    then
        printf "\n# Job output stream\n"
        "$pds_api" job_stream_output "$jobUUID"
    fi

    ((retries--))
    sleep 0.5
done

if [[ $(did_job_fail $status) == "yes" ]]
then
    printf "\n# Job output stream\n"
    "$pds_api" job_stream_output "$jobUUID"

    printf "\n# Job error stream\n"
    "$pds_api" job_stream_error "$jobUUID"
else
    echo "Return the result"
    "$pds_api" job_result "$jobUUID"
fi
