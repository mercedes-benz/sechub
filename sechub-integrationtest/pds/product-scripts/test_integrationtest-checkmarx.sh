#!/bin/bash 
# SPDX-License-Identifier: MIT

# About
# -----
# This bash script is just for testing the "integrationtest-checkmarx.sh" script directly 
# from command line - without starting an integration test.
#
# Reason: Faster development, debugging and failure search. 
#


echo "/-------------------------------------------------\\"
echo "|                                                 |"
echo "| TEST: source integrationtest-checkmarx          |"
echo "|       (will start the wrapper)                  |"
echo "|                                                 |"
echo "\\-------------------------------------------------/"
cd ..
cd ..
TEST_WORKING_DIR=$(pwd)
TEST_WORKSPACE="$TEST_WORKING_DIR/build/sechub/test_integrationtest-checkmarx"
echo "- changed to work space:"
echo "  $TEST_WORKSPACE"
echo ""
echo "- clean work space"
rm "$TEST_WORKSPACE" -rf
echo ""
echo "- start simulation of PDS call from SecHub by setting environment variables"
# ---------------
# common setup
# ---------------
export SECHUB_JOB_UUID=665dc4e8-d2de-4d2f-a3a3-2b447630b229

export PDS_JOB_UUID=125dc4e8-d2de-4d2f-a3a3-4c447630b228
export PDS_JOB_USER_MESSAGES_FOLDER="$TEST_WORKSPACE/messages"
export PDS_JOB_RESULT_FILE="$TEST_WORKSPACE/output/result.txt"
export PDS_JOB_EXTRACTED_SOURCES_FOLDER="$TEST_WORKSPACE/extracted/sources"
export PDS_SCAN_CONFIGURATION="{\"projectId\" : \"project1\"}"
export PDS_JOB_METADATA_FILE="$TEST_WORKSPACE/metadata.txt"

# ------------------------------
# checkmarx specific
# ------------------------------
export PDS_CHECKMARX_USER=checkmarx-user
export PDS_CHECKMARX_PASSWORD=checkmarx-password
export PDS_CHECKMARX_BASEURL=https://checkmarx.mock.example.org:6011

export PDS_CHECKMARX_ENGINE_CONFIGURATION_NAME=config1

export CHECKMARX_NEWPROJECT_TEAMID_MAPPING='{"entries":[{"pattern":".*project1*","replacement":"team1"}]}'
export CHECKMARX_NEWPROJECT_PRESETID_MAPPING='{"entries":[{"pattern":".*project1*","replacement":"4711","comment":"test preset id mapping project1"}]}'

# -----------------
# Mock adapters
# -----------------
export PDS_CHECKMARX_MOCKING_ENABLED=true
# enable mocked adapters to check if the parameters are as expected
# just a sanity check that parameters are loaded etc.
export SECHUB_ADAPTER_MOCK_SANITYCHECK_ENABLED=true

echo "  Exported variables - DONE"
echo ""
echo "- simulate source upload inside $PDS_JOB_EXTRACTED_SOURCES_FOLDER by creating folder"
mkdir "$PDS_JOB_EXTRACTED_SOURCES_FOLDER" -p
touch "$PDS_JOB_EXTRACTED_SOURCES_FOLDER/at-least-one-file.txt"
echo ""
echo "- start sourcing the integration test script"
echo ""

## Simulate same situation as in PDS integration test:
cd "$TEST_WORKING_DIR"
# Next line is same start path as done in integrationtest pds config file:
./../sechub-integrationtest/pds/product-scripts/integrationtest-checkmarx.sh


