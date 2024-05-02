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

options="-Dspring.profiles.active=default"

# check if the Prepare Wrapper exists
if [[ ! -f "$prepare_wrapper" ]]; then
    echo "ERROR: The Prepare wrapper file $prepare_wrapper does not exist." 1>&2
    exit 1
fi

if [[ "$PDS_DEBUG_ENABLED" = "true" ]]; then
    options="$options -Dlogging.level.org.springframework.web=DEBUG -Dlogging.level.com.mercedesbenz=DEBUG"

    echo ""
    echo "  PDS DEBUG:"
    echo "  ******************"
    echo "  - PDS_SCAN_CONFIGURATION                 : $PDS_SCAN_CONFIGURATION"
    echo ""
    echo "  - Java jar OPTIONS                       : $options"
fi

# export upload directory
export PDS_PREPARE_UPLOAD_DIRECTORY="$PDS_JOB_WORKSPACE_LOCATION/remote_data/upload"

echo ""
java -jar $options "$prepare_wrapper"



