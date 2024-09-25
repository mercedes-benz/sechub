#!/bin/bash
# SPDX-License-Identifier: MIT

# creates a sechub executor from an executor file and a sechub project

declare -r SCRIPT_PARAMETERS="<executor-config-file-name> <profile-name>"
declare -r EXECUTOR_DIRECTORY="executors"

sechub_api="../sechub-developertools/scripts/sechub-api.sh"

cd $(dirname "$0")
source 8900-helper.sh
source 8901-check-setup.sh

check_sechub_server_setup "$0" "$SCRIPT_PARAMETERS"

executor_config_file="$1"
profile="$2"

if [[ -z "$executor_config_file" ]]
then
    print_error_message "Executor config file name is missing."
    usage "$0" "$SCRIPT_PARAMETERS"
    exit 1
fi

executor_file="$EXECUTOR_DIRECTORY/$executor_config_file.json"
if [[ ! -f  "$executor_file" ]]
then
    print_error_message "Executor file $executor_file does not exist."
    usage "$0" "$SCRIPT_PARAMETERS"
    exit 1
fi

if [[ -z "$profile" ]]
then
    print_error_message "Profile name is missing."
    usage "$0" "$SCRIPT_PARAMETERS"
    exit 1
fi

# list profiles
profiles=$($sechub_api profile_list)

# simplify profile structure to array of profile ids (names)
profile_ids=$(echo "$profiles" | jq 'map(.id)')
is_profile_in_profiles=$(json_is_element_in_array "$profile" "$profile_ids")

if [[ "$is_profile_in_profiles" == "false" ]]
then
    # create executor
    executor_uuid=$($sechub_api executor_create "$executor_file")

    # create profile
    $sechub_api profile_create "$profile" "$executor_uuid"

    # show profile
    $sechub_api profile_details "$profile"

    echo "Created executor and profile"
else
    echo "Profile $profile already exists."
fi