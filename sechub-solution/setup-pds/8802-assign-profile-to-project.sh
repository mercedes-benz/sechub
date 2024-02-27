#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

# uses the sechub-api.sh script to assign an existing profile to an existing project

declare -r SCRIPT_PARAMETERS="<project> <profile>"
sechub_api="../sechub-developertools/scripts/sechub-api.sh"

cd $(dirname "$0")
source 8900-helper.sh
source 8901-check-setup.sh

check_sechub_server_setup "$0" "$SCRIPT_PARAMETERS"

project="$1"
profile="$2"

if [[ -z "$project" ]]
then
    print_error_message "Project name is missing."
    usage "$0" "$SCRIPT_PARAMETERS"
    exit 1
fi

if [[ -z "$profile" ]]
then
    print_error_message "Profile name is missing."
    usage "$0" "$SCRIPT_PARAMETERS"
    exit 1
fi

echo "project: $project"
echo "profile: $profile"

# list profiles
profiles=$($sechub_api profile_list)

# simplify profile structure to array of profile ids (names)
profile_ids=$(echo "$profiles" | jq 'map(.id)')
is_profile_in_profiles=$(json_is_element_in_array "$profile" "$profile_ids")

# list projects
projects=$($sechub_api project_list)
is_project_in_list=$(json_is_element_in_array "$project" "$projects") 

if [[ "$is_project_in_list" == "false"  ]]
then
    print_error_message "Project does not exist."
    exit 1
fi

if [[ "$is_profile_in_profiles" == "false" ]]
then
    print_error_message "Profile does not exist."
    exit 1
fi

# in case project and profile exist assign them to each other
$sechub_api project_assign_profile "$project" "$profile"

echo "Assigned profile $profile to project $project"