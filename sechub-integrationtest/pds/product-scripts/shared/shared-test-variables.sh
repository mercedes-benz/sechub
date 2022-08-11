#!/bin/bash 
# SPDX-License-Identifier: MIT

function dumpPDSVariables() {
    echo ""
    echo "Dump PDS variables (can be checked in tests):"
    echo "*********************************************"
    dumpVariable "PDS_JOB_UUID"
    dumpVariable "PDS_DEBUG_ENABLED"
    dumpVariable "PDS_TEST_KEY_VARIANTNAME"
    dumpVariable "PDS_JOB_USER_MESSAGES_FOLDER"
    dumpVariable "PDS_JOB_HAS_EXTRACTED_SOURCES"
    dumpVariable "PDS_JOB_HAS_EXTRACTED_BINARIES"
    dumpVariable "PDS_JOB_METADATA_FILE"
    dumpVariable "PDS_CONFIG_SCRIPT_TRUSTALL_CERTIFICATES_ENABLED"
}
 
function dumpVariable(){
    local variableName=$1
    echo ">${variableName}=${!variableName}"
} 