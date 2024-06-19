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

storage_options="-Dpds.storage.sharedvolume.upload.dir=$SHARED_VOLUME_UPLOAD_DIR"

# check if the Prepare Wrapper exists
if [[ ! -f "$prepare_wrapper" ]]; then
	echo "$pwd" 1>&2
    echo "ERROR: The Prepare wrapper file $prepare_wrapper does not exist." 1>&2
    exit 1
fi

if [[ "$PDS_INTEGRATIONTEST_ENABLED" = "true" ]]; then
    options="-Dspring.profiles.active=pds_integrationtest"
fi

if [[ "$PDS_DEBUG_ENABLED" = "true" ]]; then
    options="$options -Dlogging.level.org.springframework.web=DEBUG -Dlogging.level.com.mercedesbenz=DEBUG"

    echo ""
    echo "  PDS DEBUG:"
    echo "  ******************"
    echo "  - PDS_SCAN_CONFIGURATION                        : $PDS_SCAN_CONFIGURATION"
    echo "  - PDS_PREPARE_MINUTES_TO_WAIT_PREPARE_PROCESSES : $PDS_PREPARE_MINUTES_TO_WAIT_PREPARE_PROCESSES"
    echo "  - PDS_PREPARE_MODULE_ENABLED_GIT                : $PDS_PREPARE_MODULE_ENABLED_GIT"
    echo "  - PDS_PREPARE_AUTO_CLEANUP_GIT_FOLDER           : $PDS_PREPARE_AUTO_CLEANUP_GIT_FOLDER"
    echo "  - SECHUB_JOB_UUID                               : $SECHUB_JOB_UUID"
    echo "  - PDS_JOB_RESULT_FILE                           : $PDS_JOB_RESULT_FILE"
    echo "  - PDS_JOB_USER_MESSAGES_FOLDER                  : $PDS_JOB_USER_MESSAGES_FOLDER"
    echo "  - PDS_STORAGE_SHAREDVOLUME_UPLOAD_DIR           : $PDS_STORAGE_SHAREDVOLUME_UPLOAD_DIR"
    echo "  - Java jar OPTIONS                              : $options"
fi


echo ""
java -jar $storage_options $options "$prepare_wrapper"



