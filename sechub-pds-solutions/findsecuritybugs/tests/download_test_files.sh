#!/usr/bin/bash
# SPDX-License-Identifier: MIT

current_test_folder="$1"
test_file="$2"
test_folder_name="$3"
	
if [[ ! -d "$current_test_folder" ]]
then
	echo "ERROR: Target folder \"$current_test_folder\" does not exist."
	exit 1
fi

if [[ -z "$test_file" ]]
then
	echo "ERROR: No file to test provided."
	exit 1
fi

if [[ -z "$test_folder_name" ]]
then
	echo "ERROR: No test folder provided."
	exit 1
fi

cd "$current_test_folder"

echo "downloading: $test_file"
mkdir "$test_folder_name"
cd "$test_folder_name"
wget "$test_file"