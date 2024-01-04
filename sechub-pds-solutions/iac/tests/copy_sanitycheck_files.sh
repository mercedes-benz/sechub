#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

current_test_folder="$1"
	
if [[ ! -d "$current_test_folder" ]]
then
	echo "Target folder is empty"
	exit 1
fi

cd `dirname $0`

echo "copy sanity check testdata folder"
cp -r sanity-check-testdata/ "$current_test_folder/sanity-check"

