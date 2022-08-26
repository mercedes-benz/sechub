#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

declare -r SCRIPT_PARAMETERS="<project-id> <user>"

cd $(dirname "$0")
source 8900-helper.sh
source 8901-check-setup.sh

check_sechub_server_setup "$0" "$SCRIPT_PARAMETERS"

# a username needs to be at least 5 characters in length
user="pmd-user"
project="test-pmd"
executor_file_name="pmd"
profile="pds-pmd"

setup_project_user_executor_profile "$project" "$user" "$executor_file_name" "$profile"

setup_complete_message_for_tool "PMD" "$user" "$project"