#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

vulnerable_apps=(
    "https://github.com/globocom/secDevLabs.git",
    "https://github.com/OWASP/crAPI"
)

current_test_folder="$1"
vulnerable_repo="$2"
	
if [[ -z "$current_test_folder" ]]
then
	echo "Target folder is empty"
	exit 1
fi

if [[ -z "$vulnerable_repo" ]]
then
	echo "No vulneable application repository provided"
	exit 1
fi

cd "$current_test_folder"

echo "cloning: $vulnerable_repo"
git clone "$vulnerable_repo"