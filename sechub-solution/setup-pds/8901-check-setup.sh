#!/usr/bin/bash
# SPDX-License-Identifier: MIT

source "8900-helper.sh"
# SPDX-License-Identifier: MIT

script_name=""
parameters=""

function parameter_missing() {
    local message="$1"

    print_error_message "$message"

    usage "$script_name" "$parameters"

    exit 1
}

# check if variables are set and sechub server is alive
function check_sechub_server_setup() {
    script_name="$1"
    parameters="$2"

    # check if required environment variables are set
    sechub_api="../../sechub-developertools/scripts/sechub-api.sh"

    if [[ -z "$SECHUB_SERVER" ]]
    then
        parameter_missing "Environment variable SECHUB_SERVER missing."
    fi

    if [[ -z "$SECHUB_USERID" ]]
    then
        parameter_missing "Environment variable SECHUB_USERID missing."
    fi

    if [[ -z "$SECHUB_APITOKEN" ]]
    then
        parameter_missing "Environment variable SECHUB_APITOKEN missing."
    fi

  # check if sechub server is running
    alive_check=$($sechub_api alive_check)

    if [[ -z "$alive_check" ]]
    then
        printf "\n[ERROR] The SecHub server $SECHUB_SERVER is not running.\n"
        printf "Check if the SecHub server is running.\n"
        exit 3
    fi
}
