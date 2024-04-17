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
user="gosec"
project="test-gosec"
executor_file_name="gosec"
profile="pds-gosec"

# main setup execution
setup_project_user_executor_profile "$project" "$user" "$executor_file_name" "$profile"

# print sechub scan usage message
setup_complete_message_for_tool "GoSec" "$user" "$project"