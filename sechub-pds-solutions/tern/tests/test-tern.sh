#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

BUILD_FOLDER="build/"
TOOLS="tools/"
PDS_TOOLS_CLI_VERSION="0.1.0"
PDS_API='../../../sechub-developertools/scripts/pds-api.sh'


DEFAULT_PDS_SERVER="https://localhost:8444"
DEFAULT_PDS_USERID="admin"
DEFAULT_PDS_APITOKEN="pds-apitoken"

function did_job_fail() {
    local status="$1"
    job_failed="no"

    if [[ $status == "FAILED" ]]
    then
        job_failed="yes"
    fi

    echo $job_failed
}

function is_job_finished() {
    local status="$1"
    job_finished="no"

    if [[ $status == "DONE" || $status == "FINISHED" || $status == "FAILED" ]]
    then
        job_finished="yes"
    fi

    echo $job_finished
}

function log_step() {
    local message="$1"

    printf ">> %s\n" "$message"
}

function log_error() {
    local message="$1"

    printf ">> ERROR: %s\n" "$message"
}

function start_tern_docker() {
    log_step "Starting tern"
    nohup ../01-start-single-docker-compose.sh &
}

function stop_tern_docker() {
    log_step "Stopping tern"
    docker compose --file ../docker-compose_pds_tern.yaml down
}

function wait_for_tern_to_be_alive() {
    export PDS_SERVER="https://localhost:8444"
    export PDS_USERID="admin"
    export PDS_APITOKEN="pds-apitoken"

    is_alive="no"
    retries=120

    while [[ $retries -ge 0 && "$is_alive" == "no" ]]
    do
        check_alive=$( $PDS_API check_alive )

        if [[ ! -z "$check_alive" ]]
        then
            is_alive="yes"
        else
            ((retries--))
            sleep 1s
        fi
    done

    echo "$is_alive"
}

function build_image() {
    log_step "Building image"
    local name="$1"
    local dockerfile="$2"

    docker build --tag "$name" --file "$dockerfile" . 
}

function setup() {
    log_step "Setup"
    helper_download_pds_tools
    mkdir "$BUILD_FOLDER"
}

function helper_download_pds_tools() {
    log_step "Downloading PDS Tools"
    if [[ -f "$TOOLS/sechub-pds-tools-cli-$PDS_TOOLS_CLI_VERSION.jar" ]]
    then
        echo "PDS Tools already downloaded. Not downloading again."
        return 0
    fi

    mkdir "$TOOLS"
    cd "$TOOLS"
    wget "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_TOOLS_CLI_VERSION-pds-tools/sechub-pds-tools-cli-$PDS_TOOLS_CLI_VERSION.jar"
    wget "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_TOOLS_CLI_VERSION-pds-tools/sechub-pds-tools-cli-$PDS_TOOLS_CLI_VERSION.jar.sha256sum"
    sha256sum --check "sechub-pds-tools-cli-$PDS_TOOLS_CLI_VERSION.jar.sha256sum"

    cd ..
}

function package_image() {
    log_step "Packaging docker image"

    local image_name="$1"
    local image_path="${image_name}.tar"
    local scan_type="licenseScan"
    local sechub_configuration_file_path="${BUILD_FOLDER}/sechub-config-${image_name}.json"
    
    save_docker_image "$image_name" "$image_path"

    cp "sechub-config-template.json" "$sechub_configuration_file_path"
    sed --in-place --expression "s|_IMAGE_NAME_|${image_name}|g; s|_IMAGE_PATH_|${image_path}|g; s|_SCAN_TYPE_|${scan_type}|g" "$sechub_configuration_file_path"

    java -jar "$TOOLS/sechub-pds-tools-cli-$PDS_TOOLS_CLI_VERSION.jar" --generate "$sechub_configuration_file_path" "$scan_type" "$BUILD_FOLDER"
}

function save_docker_image() {
    log_step "Saving docker image to file"

    local image_name="$1"
    local image_path="$2"
    
    docker save --output "${BUILD_FOLDER}/$image_path" "$image_name"
}

function create_job() {
    local job_creation_file="$1"

    # move parts
}

function upload_image_and_scan() {
    log_step "Uploading image and scanning"

    local file_to_upload="$1"

    export PDS_SERVER="https://localhost:8444"
    export PDS_USERID="admin"
    export PDS_APITOKEN="pds-apitoken"

    # create job

    json_config="pds-alpine-config.json"
    json_job_creation=$( $PDS_API create_job_from_json "$json_config" )

    # check the result for a jobUUID
    echo "$json_job_creation"

    jobUUID=$( echo "$json_job_creation" | jq '.jobUUID' | tr -d \")

    if [[ "$jobUUID" == "null" ]]
    then
        log_error "Unable to create job."
        echo "$json_job_creation"
    else
        # upload file
        echo "Uploading file: $file_to_upload"
        "$PDS_API" upload "$jobUUID" "$file_to_upload"

        # mark job as ready to start
        "$PDS_API" mark_job_ready_to_start "$jobUUID" 
    fi

    # Check the status of the job
    retries=1200
    status=""

    while [[ $retries -ge 0 && $(is_job_finished $status) == "no" ]]
    do
        status=$($PDS_API job_status "$jobUUID" | jq '.state' | tr -d \")
        echo "Job status: $status"

        if (( $retries % 10 == 0 ))
        then
            printf "\n# Job output stream\n"
            "$PDS_API" job_stream_output "$jobUUID"
        fi

        ((retries--))
        sleep 0.5s
    done

    printf "\n# Return the result\n"
    "$PDS_API" job_result "$jobUUID"
}

function tear_down() {
    log_step "Tear down. Cleanup"
    remove_nohup_log_file
    #remove_build_folder
}

function remove_build_folder() {
    rm -rf "$BUILD_FOLDER"
}

function remove_nohup_log_file() {
    rm "nohup.out"
}

# start tern docker
# wait for pds to be alive

# for image in images
#   build image
#   package image -> pds cli tool
#   upload to tern
#   compare results -> remember result
#
# stop tern docker


start_tern_docker
setup

if [[ $( wait_for_tern_to_be_alive ) == "yes" ]]
then
    # put the steps into a separate script
    log_step "Waiting successful. PDS Tern is alive"
    name_docker_image_to_test="sechub-test-alpine"
    build_image "$name_docker_image_to_test" "alpine/Alpine.dockerfile"
    package_image "$name_docker_image_to_test"
    upload_image_and_scan "build/binaries.tar"
else
    log_error "Unable to connect to PDS Tern."
fi

stop_tern_docker
tear_down