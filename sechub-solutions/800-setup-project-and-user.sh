#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

function usage() {
    local script_name=
    echo "`basename $0` <project-id> <user>"
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

function parameter_missing() {
    MESSAGE="$1"

    printf "[ERROR] $MESSAGE\n"

    usage

    exit 1
}

sechub_api="../sechub-developertools/scripts/sechub-api.sh"

if [[ -z "$SECHUB_SERVER" ]]
then
    parameter_missing "Environment variable SECHUB_SERVER missing."
fi

alive_check=$($sechub_api alive_check)

if [[ -z $alive_check ]]
then
    printf "\n[ERROR] The SecHub server $SECHUB_SERVER is not to running.\n"
    printf "[ERROR] Check if the PDS is running.\n"
    exit 3
fi

# project name
project_id="test"
user="user"

# user list
user_list=$($sechub_api user_list $user) # | jq -e ". as $users | $user | IN($users)" )

echo "$user_list"
# signup user
#"$sechub_api user_signup $user"

# accept user signup
#"$sechub_api user_signup_accept $user"

# create project
#"$sechub_api project_create $project_name $user"

# assign user
#"$sechub_api project_assign_user $project_name $user"