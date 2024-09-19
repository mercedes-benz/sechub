#!/bin/bash
# SPDX-License-Identifier: MIT

declare -r checkmarx_wrapper="$TOOL_FOLDER/sechub-wrapper-checkmarx.jar"

options="-Dspring.profiles.active=default"

echo "######################################################"
echo "# Starting Checkmarx PDS-solution                    #"
echo "######################################################"
echo "- SECHUB_JOB_UUID : $SECHUB_JOB_UUID"

# check if the Checkmarx Wrapper exists
if [[ ! -f "$checkmarx_wrapper" ]]; then
    echo "ERROR: The Checkmarx wrapper file $checkmarx_wrapper does not exist." 1>&2
    exit 1
fi

if [[ "$PDS_WRAPPER_REMOTE_DEBUGGING_ENABLED" = "true" ]]; then
    options="$options -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000"
fi

if [[ "$PDS_DEBUG_ENABLED" = "true" ]]; then
    options="$options -Dlogging.level.org.springframework.web=DEBUG -Dlogging.level.com.mercedesbenz=DEBUG"

    echo ""
    echo "  PDS DEBUG:"
    echo "  ******************"
    echo "  - PDS_CHECKMARX_MOCKING_ENABLED          : $PDS_CHECKMARX_MOCKING_ENABLED"
    echo "  - PDS_CHECKMARX_BASEURL                  : $PDS_CHECKMARX_BASEURL"
    echo "  - PDS_CHECKMARX_ENGINE_CONFIGURATION_NAME: $PDS_CHECKMARX_ENGINE_CONFIGURATION_NAME"
    echo "  - PDS_SCAN_CONFIGURATION                 : $PDS_SCAN_CONFIGURATION"
    echo "  - CHECKMARX_NEWPROJECT_TEAMID_MAPPING    : $CHECKMARX_NEWPROJECT_TEAMID_MAPPING"
    echo "  - CHECKMARX_NEWPROJECT_PRESETID_MAPPING  : $CHECKMARX_NEWPROJECT_PRESETID_MAPPING"
    echo ""
    echo "  - Java jar OPTIONS                       : $options"
fi

# Info about next java call:
# - 0.0.0 because we have no special version
# - Versioning is done by docker images + commit ids in name
echo ""
java -jar $options "$checkmarx_wrapper"

                                                          
