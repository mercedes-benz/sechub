#!/usr/bin/bash
# SPDX-License-Identifier: MIT

BUILD_FOLDER="build"
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
}

function create_build_folder() {
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
    local pds_job_configuration_file_path="${BUILD_FOLDER}/pds-config-${image_name}.json"
    
    save_docker_image "$image_name" "$image_path"
    create_sechub_config "$image_name" "$image_path" "$sechub_configuration_file_path"

    generate_binaries_tar "$sechub_configuration_file_path" "$scan_type"

    create_pds_job_config "$pds_job_configuration_file_path"
}

function generate_binaries_tar() {

    local sechub_configuration_file_path="$1"
    local scan_type="$2"

    java -jar "$TOOLS/sechub-pds-tools-cli-$PDS_TOOLS_CLI_VERSION.jar" --generate "$sechub_configuration_file_path" "$scan_type" "$BUILD_FOLDER"
}
function save_docker_image() {
    log_step "Saving docker image to file"

    local image_name="$1"
    local image_path="$2"
    
    docker save --output "${BUILD_FOLDER}/$image_path" "$image_name"
}

function create_job() {
    local job_config_file="$1"

    json_job_creation=$( $PDS_API create_job_from_json "$job_config_file" )

    jobUUID=$( echo "$json_job_creation" | jq '.jobUUID' | tr -d \")

    echo "$jobUUID"
}

function create_sechub_config() {
    local image_name="$1"
    local image_path="$2"
    local sechub_configuration_file_path="$3"

    cp "sechub-config-template.json" "$sechub_configuration_file_path"
    sed --in-place --expression "s|_IMAGE_NAME_|${image_name}|g; s|_IMAGE_PATH_|${image_path}|g; s|_SCAN_TYPE_|${scan_type}|g" "$sechub_configuration_file_path"
}

function create_pds_job_config() {
    local pds_job_configuration_file_path="$1"

    pds_scan_configuration=$( jq --raw-output '.parameters[] | select(.key == "pds.scan.configuration").value' "${BUILD_FOLDER}/pdsJobData.json" )

    cp "pds-job-config-template.json" "$pds_job_configuration_file_path"

    tmp_file="$pds_job_configuration_file_path.tmp"
    cp "$pds_job_configuration_file_path" "$tmp_file"
    jq --arg pdsscanconfiguration "$pds_scan_configuration" '(.parameters[] | select(.key == "pds.scan.configuration")).value |= $pdsscanconfiguration' "$tmp_file" > "$pds_job_configuration_file_path"
    rm "$tmp_file"
}

function upload_image_and_scan() {
    log_step "Uploading image and scanning"

    local file_to_upload="$1"
    local pds_job_configuration_file_path="$2"
    local result_file_path="$3"

    export PDS_SERVER="https://localhost:8444"
    export PDS_USERID="admin"
    export PDS_APITOKEN="pds-apitoken"


    jobUUID=$( create_job "$pds_job_configuration_file_path" )

    if [[ "$jobUUID" == "null" ]]
    then
        log_error "Unable to create job."
        return 1
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

    printf "\n# Writing result to: $result_file_path\n"
    "$PDS_API" job_result "$jobUUID" > "$result_file_path"
}

function tear_down() {
    log_step "Tear down. Cleanup"
    remove_nohup_log_file
}

function remove_build_folder() {
    rm -rf "$BUILD_FOLDER"
}

function remove_nohup_log_file() {
    rm "nohup.out"
}

function compare_results() {
    log_step "Compare results."

    local expected_result_file_path="$1"
    local result_file_path="$2"

    diff "$expected_result_file_path" "$result_file_path"

    if [[ "$?" -ne 0 ]]
    then
        echo "Error: files not equal"
        return 1
    fi

    return 0
}

function run_test() {
    local name="$1"
    local dockerfile="$2"

    name_docker_image_to_test="$1"

    #result_file_path="$name_docker_image_to_test.spdx.json"
    result_file_path="$BUILD_FOLDER/$name_docker_image_to_test.spdx.json"
    expected_result_file_path="expected-results/$name_docker_image_to_test.spdx.json"

    build_image "$name_docker_image_to_test" "$dockerfile"
    package_image "$name_docker_image_to_test"
    upload_image_and_scan "build/binaries.tar" "${BUILD_FOLDER}/pds-config-${name_docker_image_to_test}.json" "$result_file_path"
    #compare_results "$expected_result_file_path" "$result_file_path"
}