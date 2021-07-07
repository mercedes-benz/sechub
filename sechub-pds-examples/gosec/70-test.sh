#!/usr/bin/env bash

file_to_upload="$1"
retries=20

function is_job_finished () {
    local status="$1"
    job_finished="no"

    if [[ $status == "DONE" || $status == "FINISHED" ]]
    then
        job_finished="yes"
    fi

    echo $job_finished
}

if [[ -z "$file_to_upload" ]]
then
    echo "Please provide file to upload"
    exit 1
fi

pds_api="../../sechub-developertools/scripts/pds-api.sh"
jobUUID=`$pds_api create_job PDS_GOSEC "288607bf-ac81-4088-842c-005d5702a9e9" | jq '.jobUUID' | tr -d \"`

"$pds_api" upload_zip "$jobUUID" /home/developer/pds/abc.zip
"$pds_api" mark_job_ready_to_start "$jobUUID"

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

