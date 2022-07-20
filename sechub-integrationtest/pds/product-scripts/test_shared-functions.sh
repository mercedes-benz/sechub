#!/bin/bash 
# SPDX-License-Identifier: MIT

# About
# -----
# This bash script is just for testing the "shared-functions.sh" script directly 
# from command line - without starting an integration test.
#
# Reason: Faster devlelopment, debugging and failure search. 
#


DEBUG=true
PDS_TEST_KEY_VARIANTNAME="pds.test.key.variantname.value"

source ./shared-functions.sh
set -e

echo "testing shared functions - only for debugging the testscripts itself."

echo "/-------------------------------------------------\\"
echo "|                                                 |"
echo "| TEST: mergeFolderFilesRecursivelyIntoResultFile |"
echo "|                                                 |"
echo "\\-------------------------------------------------/"


TEST_DAT_TAR_LOCATION="./../../src/test/resources/pds/codescan/upload/tarfile_contains_different_finding_files_in_different_data_sections.tar"

BUILD_TMP_FOLDER="./../../build/tmp"
#ls $BUILD_TMP_FOLDER
TEST_FOLDER="$BUILD_TMP_FOLDER/shared-functions-test"
TEST_FOLDER_EXTRACTED="$TEST_FOLDER/extracted"
TEST_FOLDER_RESULT="$TEST_FOLDER/result.txt"

rm -fr $TEST_FOLDER
mkdir -p $TEST_FOLDER_EXTRACTED
debug "> dropped old tmp data from: $TEST_FOLDER" $DEBUG
tar -xf $TEST_DAT_TAR_LOCATION --directory $TEST_FOLDER_EXTRACTED
debug "> extracted $TEST_DAT_TAR_LOCATION into : $TEST_FOLDER" $DEBUG

mergeFolderFilesRecursivelyIntoResultFile "test-sources" $TEST_FOLDER_EXTRACTED $TEST_FOLDER_RESULT $DEBUG

echo ">>>> RESULT"
if [[ "$DEBUG" = "true" ]]; then
    echo "$TEST_FOLDER contains:"
    ls -all $TEST_FOLDER 
fi
echo "$TEST_FOLDER_RESULT contains:"
cat $TEST_FOLDER_RESULT

echo "/-------------------------------------------------\\"
echo "|                                                 |"
echo "| TEST: message methods                           |"
echo "|                                                 |"
echo "\\-------------------------------------------------/"
PDS_JOB_USER_MESSAGES_FOLDER=$TEST_FOLDER/output/messages
rm $PDS_JOB_USER_MESSAGES_FOLDER -rf

mkdir $PDS_JOB_USER_MESSAGES_FOLDER -p

infoMessage "this is an info message"
warnMessage "this is a warning message"
errorMessage "this is an error message
    with multiple lines... 
"
echo "Messages at: $PDS_JOB_USER_MESSAGES_FOLDER"
echo "----------------------------------------------------------------------------"
ls $PDS_JOB_USER_MESSAGES_FOLDER 
echo "----------------------------------------------------------------------------"

echo "/-------------------------------------------------\\"
echo "|                                                 |"
echo "| TEST: dump PDS variables                        |"
echo "|                                                 |"
echo "\\-------------------------------------------------/"

dumpPDSVariables