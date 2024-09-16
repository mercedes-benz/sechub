#!/bin/bash
# SPDX-License-Identifier: MIT

# creates project with user variable as owner and assign user to project using the sechub-api.sh

declare -r SCRIPT_PARAMETERS="<project-id> <user>"
sechub_api="../sechub-developertools/scripts/sechub-api.sh"

cd $(dirname "$0")
source 8900-helper.sh
source 8901-check-setup.sh

check_sechub_server_setup "$0" "$SCRIPT_PARAMETERS"

project="$1"
user="$2"

if [[ -z "$project" ]]
then
    print_error_message "Project name is missing."
    usage "$0" "$SCRIPT_PARAMETERS"
    exit 1
fi

if [[ -z "$user" ]]
then
    print_error_message "User name is missing."
    usage "$0" "$SCRIPT_PARAMETERS"
    exit 1
fi

echo "user: $user"
echo "project: $project"

# list users
users=$($sechub_api user_list)
is_user_in_list=$(json_is_element_in_array "$user" "$users")

if [[ "$is_user_in_list" == "true" ]]
then
    echo "User $user exists already."
else
    echo "Adding user: $user"

    # signup user
    $sechub_api user_signup "$user" "$user@example.org"

    # accept user signup
    $sechub_api user_signup_accept "$user"
fi

# list projects
projects=$($sechub_api project_list)
is_project_in_list=$(json_is_element_in_array "$project" "$projects") 

if [[ "$is_project_in_list" == "true" ]]
then
    echo "Project $project exists already."
else
    echo "Creating project $project."

    # create project with owner
    $sechub_api project_create "$project" "$user"
fi

# assign user to project
$sechub_api project_assign_user "$project" "$user"