#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

declare -r SCRIPT_PARAMETERS="<project-id> <user>"

cd $(dirname "$0")
source 8900-helper.sh
source 8901-check-setup.sh

# check if sechub server is alive
check_sechub_server_setup "$0" "$SCRIPT_PARAMETERS"

# set variables
# a username needs to be at least 5 characters in length
# executor_file_name must be named after your executor file in /executors
user="prepare-user"
project="test-prepare"
executor_file_name="prepare"
profile="pds-prepare"
echo ""
# defining a second profile with the tool to scan
second_profile="pds-gosec"
second_executor_filename="gosec"
echo "Second profile: $second_profile with executor $second_executor_filename"
echo""

# main setup execution
setup_project_user_executor_profile "$project" "$user" "$executor_file_name" "$profile"

setup_second_executor_profile_to_existing_project "$project" "$second_executor_filename" "$second_profile"

# print sechub scan usage message
setup_complete_message_for_tool "Prepare" "$user" "$project"
