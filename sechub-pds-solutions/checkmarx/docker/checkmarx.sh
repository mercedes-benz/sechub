#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

echo "######################################################"
echo "# Starting Checkmarx PDS-solution                    #"
echo "######################################################"
echo "- SECHUB_JOB_UUID : $SECHUB_JOB_UUID"

options="";

if [[ "$PDS_DEBUG_ENABLED" = "true" ]]; then
    echo ""
    echo "  PDS DEBUG:"
    echo "  ******************"
    echo "  - PDS_CHECKMARX_MOCKING_ENABLED          : $PDS_CHECKMARX_MOCKING_ENABLED"
    echo "  - PDS_CHECKMARX_BASE_URL                 : $PDS_CHECKMARX_BASE_URL"
    echo "  - PDS_CHECKMARX_ENGINE_CONFIGURATION_NAME: $PDS_CHECKMARX_ENGINE_CONFIGURATION_NAME"
    echo "  - PDS_SCAN_CONFIGURATION                 : $PDS_SCAN_CONFIGURATION"
    echo "  - CHECKMARX_NEWPROJECT_TEAMID_MAPPING    : $CHECKMARX_NEWPROJECT_TEAMID_MAPPING"
    echo "  - CHECKMARX_NEWPROJECT_PRESETID_MAPPING  : $CHECKMARX_NEWPROJECT_PRESETID_MAPPING"
    
    options="$options -Dlogging.level.com.mercedesbenz.sechub=DEBUG"
fi


options="$options -Dspring.profiles.active=default"

# Info about next java call:
# - 0.0.0 because we have no special version
# - Versioning is done by docker images + commit ids in name
if [[ "$PDS_DEBUG_ENABLED" = "true" ]]; then
    echo "  - Java jar OPTIONS                       :$options"
fi

echo ""
java -jar $options $TOOL_FOLDER/sechub-pds-wrapper-checkmarx.jar  

                                                          
