#!/bin/bash 
# SPDX-License-Identifier: MIT

# About
# -----
# This bash script is just for testing the "integrationtest-codescan.sh" script directly 
# from command line - without starting an integration test.
#
# Reason: Faster development, debugging and failure search. 
#


echo "/-------------------------------------------------\\"
echo "|                                                 |"
echo "| TEST: source integrationtest-codescan           |"
echo "|              Variant: k                         |"
echo "|                                                 |"
echo "\\-------------------------------------------------/"
cd ..
cd ..
TEST_WORKING_DIR=$(pwd)
TEST_WORKSPACE="$TEST_WORKING_DIR/build/sechub/test_integrationtest-codescan"
echo "- changed to working directory:"
echo "  $TEST_WORKING_DIR"
echo ""
echo "- clean working directory"
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
export PDS_TEST_KEY_VARIANTNAME=k
export PDS_JOB_EVENTS_FOLDER="$TEST_WORKSPACE/events"

SIMULATE_CANCELREQUEST=false

echo "  Exported variables - DONE"
echo ""
echo "  Create missing directories and files"
echo "- create workspace output folder"
mkdir "$TEST_WORKSPACE/output/" -p

echo "- create workspace messages folder"
mkdir "$PDS_JOB_USER_MESSAGES_FOLDER" -p

echo "- create workspace events folder"
mkdir "$PDS_JOB_EVENTS_FOLDER" -p


if [[ "$SIMULATE_CANCELREQUEST" = "true" ]]; then
	touch "$PDS_JOB_EVENTS_FOLDER/cancel_requested.json"
  	echo "- YES cancel event was simulated"
else
  	echo "- NO cancel event was simulated"
fi

echo "- simulate source upload inside $PDS_JOB_EXTRACTED_SOURCES_FOLDER by creating folder"
mkdir "$PDS_JOB_EXTRACTED_SOURCES_FOLDER" -p
touch "$PDS_JOB_EXTRACTED_SOURCES_FOLDER/at-least-one-file.txt"
echo ""
echo "- start sourcing the integration test script"
echo ""
cd "$TEST_WORKING_DIR/"
echo "no inside directory: $(pwd)" 
./pds/product-scripts/integrationtest-codescan.sh

