#!/usr/bin/bash
# SPDX-License-Identifier: MIT

prepare_wrapper="$TOOL_FOLDER/sechub-wrapper-prepare.jar"

echo ""
echo "---------"
echo "PDS Setup"
echo "---------"
echo ""
echo "SecHub Job UUID: $SECHUB_JOB_UUID"
echo "PDS Job UUID: $PDS_JOB_UUID"
echo ""

# check if the Prepare Wrapper exists
if [[ ! -f "$prepare_wrapper" ]]; then
    echo "ERROR: The Prepare wrapper file $prepare_wrapper does not exist." 1>&2
    exit 1
fi

# export upload directory
export PDS_PREPARE_UPLOAD_FOLDER_DIRECTORY="$PDS_JOB_WORKSPACE_LOCATION/remote_data"

echo ""
java -jar "$prepare_wrapper"



