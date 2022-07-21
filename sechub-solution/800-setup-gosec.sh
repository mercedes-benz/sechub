#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

declare -r SCRIPT_PARAMETERS="<project-id> <user>"
sechub_api="../sechub-developertools/scripts/sechub-api.sh"

current_directory="$(dirname "$0")"
source "$current_directory/8900-helper.sh"
source "$current_directory/8901-check-setup.sh"

check_sechub_server_setup "$0" "$SCRIPT_PARAMETERS"

user="gosec"
project="test-gosec"
executor_file_name="gosec"
profile="pds-gosec"

./8000-setup-project-and-user.sh "$project" "$user"
./8100-create-executor-and-profile.sh "$executor_file_name" "$profile"
./8101-assign-profile-to-project.sh "$project" "$profile"

echo "Setup of gosec complete"
echo "Setup:"
echo "user: $user"
echo "project: $project"
echo "sechub -project $project scan"