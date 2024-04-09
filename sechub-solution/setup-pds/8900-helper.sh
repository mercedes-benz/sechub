#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

# checks if a string is a json array
function json_is_element_in_array() {
    local element="$1"
    local elements="$2"

    is_element_in_elements=$(echo "$elements" | jq --exit-status --arg element "$element" '. as $elements | $element | IN($elements[])')

    echo "$is_element_in_elements"
}

# prints usage information for environment variables
function usage() {
    local script_name="$1"
    local parameters="$2"

    echo "`basename $script_name` $parameters"
    echo ""
    
    cat <<'USAGE'
# Please set the environment variables:

export SECHUB_SERVER=https://<server>:<port>
export SECHUB_USERID=<username>
export SECHUB_APITOKEN=<password>

# Example:

export SECHUB_SERVER=https://localhost:8443
export SECHUB_USERID=admin
export SECHUB_APITOKEN='myTop$ecret!'
USAGE
}

function print_error_message() {
    local message="$1"

    printf "[ERROR] $message\n"
}

# prints usage message for sechub client if the setup of project, user, profile and executor was successful
function setup_complete_message_for_tool() {
    local tool="$1"
    local user="$2"
    local project="$3"

    printf "\nSetup of $tool complete:\n\n"
    echo "- user: $user"
    echo "- project: $project"
    printf "\nsechub -project $project scan\n"
}

# automatically setting up a project, a user, an executor and a profile at the locally running sechub server
# after the setup the sechub client can be used to scan
function setup_project_user_executor_profile() {
    local project="$1"
    local user="$2"
    local executor_file_name="$3"
    local profile="$4"

    ./8800-setup-project-and-user.sh "$project" "$user"
    ./8801-create-executor-and-profile.sh "$executor_file_name" "$profile"
    ./8802-assign-profile-to-project.sh "$project" "$profile"
}
