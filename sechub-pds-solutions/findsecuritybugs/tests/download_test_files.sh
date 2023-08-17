#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

current_test_folder="$1"
test_file="$2"
test_folder_name="$3"
	
if [[ ! -d "$current_test_folder" ]]
then
	echo "Target folder is empty"
	exit 1
fi

if [[ -z "$test_file" ]]
then
	echo "No file to test provided"
	exit 1
fi

if [[ -z "$test_folder_name" ]]
then
	echo "No file to test provided"
	exit 1
fi

cd "$current_test_folder"

echo "downloading: $test_file"
mkdir "$test_folder_name"
cd "$test_folder_name"
wget "$test_file"