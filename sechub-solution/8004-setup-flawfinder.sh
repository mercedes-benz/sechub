#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

declare -r SCRIPT_PARAMETERS="<project-id> <user>"
sechub_api="../sechub-developertools/scripts/sechub-api.sh"

current_directory="$(dirname "$0")"
source "$current_directory/8900-helper.sh"
source "$current_directory/8901-check-setup.sh"

check_sechub_server_setup "$0" "$SCRIPT_PARAMETERS"

user="flawfinder"
project="test-flawfinder"
executor_file_name="flawfinder"
profile="pds-flawfinder"

./8800-setup-project-and-user.sh "$project" "$user"
./8801-create-executor-and-profile.sh "$executor_file_name" "$profile"
./8802-assign-profile-to-project.sh "$project" "$profile"

echo "Setup of flawfinder complete"
echo "Setup:"
echo "user: $user"
echo "project: $project"
echo "sechub -project $project scan"