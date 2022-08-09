#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

declare -r SCRIPT_PARAMETERS="<project-id> <user>"

cd $(dirname "$0")
source 8900-helper.sh
source 8901-check-setup.sh

check_sechub_server_setup "$0" "$SCRIPT_PARAMETERS"

user="mobsfscan"
project="test-mobsfscan"
executor_file_name="mobsfscan"
profile="pds-mobsfscan"

setup_project_user_executor_profile "$project" "$user" "$executor_file_name" "$profile"

setup_complete_message_for_tool "mobsfscan"
echo "Setup:"
echo "user: $user"
echo "project: $project"
echo "sechub -project $project scan"