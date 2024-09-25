#!/bin/bash
# SPDX-License-Identifier: MIT

current_test_folder="$1"
vulnerable_repo="$2"
	
if [[ ! -d "$current_test_folder" ]]
then
	echo "Target folder is empty"
	exit 1
fi

if [[ -z "$vulnerable_repo" ]]
then
	echo "No vulnerable application repository provided"
	exit 2
fi

cd "$current_test_folder"

echo "cloning: $vulnerable_repo"
git clone "$vulnerable_repo"