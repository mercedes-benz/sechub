#!/bin/bash 
# SPDX-License-Identifier: MIT

# About
# -----
# This bash script is just for testing the "shared-functions.sh" script directly 
# from command line - without starting an integration test.
#
# Reason: Faster devlelopment, debugging and failure search. 
#


DEBUG=false
PDS_TEST_KEY_VARIANTNAME="pds.test.key.variantname.value"

source ./shared-functions.sh
set -e

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

rm -fr "$TEST_FOLDER"
mkdir -p "$TEST_FOLDER_EXTRACTED"
debug "> dropped old tmp data from: $TEST_FOLDER" $DEBUG
tar -xf "$TEST_DAT_TAR_LOCATION" --directory "$TEST_FOLDER_EXTRACTED"
debug "> extracted $TEST_DAT_TAR_LOCATION into : $TEST_FOLDER" $DEBUG

mergeFolderFilesRecursivelyIntoResultFile "test-sources" "$TEST_FOLDER_EXTRACTED" "$TEST_FOLDER_RESULT" $DEBUG

echo ">>>> RESULT"
if [[ "$DEBUG" = "true" ]]; then
    echo "$TEST_FOLDER contains:"
    ls -all "$TEST_FOLDER" 
fi
echo "$TEST_FOLDER_RESULT contains:"
cat "$TEST_FOLDER_RESULT"

echo "/-------------------------------------------------\\"
echo "|                                                 |"
echo "| TEST: message methods                           |"
echo "|                                                 |"
echo "\\-------------------------------------------------/"
PDS_JOB_USER_MESSAGES_FOLDER="$TEST_FOLDER/output/messages"
rm -rf "$PDS_JOB_USER_MESSAGES_FOLDER"

mkdir -p "$PDS_JOB_USER_MESSAGES_FOLDER"

# next line uses messaging function (included by hared-functions.sh and being part of documentation)
source ./shared/shared-messaging-referenced-in-documentation-as-example-usage.sh

echo "Messages at: $PDS_JOB_USER_MESSAGES_FOLDER"
echo "----------------------------------------------------------------------------"
ls "$PDS_JOB_USER_MESSAGES_FOLDER" 
echo "----------------------------------------------------------------------------"

echo "Attention! This should look similar to:"
cat ./shared/shared-messaging-referenced-in-documentation-as-example-output.txt

echo "/-------------------------------------------------\\"
echo "|                                                 |"
echo "| TEST: dump PDS variables                        |"
echo "|                                                 |"
echo "\\-------------------------------------------------/"

dumpPDSVariables
echo "/-------------------------------------------------\\"
echo "|                                                 |"
echo "| TEST: constants                                 |"
echo "|                                                 |"
echo "\\-------------------------------------------------/"

if [[ "${FUNCTION_RESULT_TRUE}" != "0" ]]; then
   echo "FAILED: FUNCTION_RESULT_TRUE :'${FUNCTION_RESULT_TRUE}' instead of 0"
   exit 3
fi

echo "- constants defined as expected"

if [[ "${FUNCTION_RESULT_FALSE}" != "1" ]]; then
   echo "FAILED: FUNCTION_RESULT_FALSE  is :'${FUNCTION_RESULT_FALSE}' instead of 1"
   exit 3
fi

echo "/-------------------------------------------------\\"
echo "|                                                 |"
echo "| TEST: check events handled by scripts functions |"
echo "|                                                 |"
echo "\\-------------------------------------------------/"
PDS_JOB_EVENTS_FOLDER="$TEST_FOLDER/events"
mkdir "$PDS_JOB_EVENTS_FOLDER" 

touch "$PDS_JOB_EVENTS_FOLDER/cancel_requested.json"
touch "$PDS_JOB_EVENTS_FOLDER/other.json"

echo "Events found in $PDS_JOB_EVENTS_FOLDER"
ls -l "$PDS_JOB_EVENTS_FOLDER" 

## ---------------------------
## eventExists 
## ---------------------------
echo ">>> check eventExists works"
if eventExists "cancel_requested" ; then
    echo "- OK: cancel_requested event found"
else 
    echo "FAILED: cancel_requested event not found!"
    exit 3
fi

if eventExists "other" ; then
    echo "- OK: other event found"
else 
    echo "FAILED: other event not found!"
    exit 3
fi

if eventExists "not_existing" ; then
    echo "FAILED: not_existing event was found!?!?!"
    exit 3
else 
    echo "- OK: not_existing event NOT found"
fi
## ---------------------------
## eventNotExists  
## ---------------------------
echo ">>> check eventNotExists works"
if eventNotExists "cancel_requested" ; then
    echo "FAILED: cancel_requested event not found!"
    exit 4
else 
    echo "- OK: cancel_requested event found"
fi

if eventNotExists "other" ; then
    echo "FAILED: other event not found!"
    exit 4
else 
    echo "- OK: other event found"
fi

if eventNotExists "not_existing" ; then
    echo "- OK: not_existing event NOT found"
else 
    echo "FAILED: not_existing event was found!?!?!"
    exit 4
fi
## ---------------------------
## waitForEventAndSendMessage 
## ---------------------------
echo ">>> check waitForEventAndSendMessage works"
if waitForEventAndSendMessage "cancel_requested" 0.1 10; then
    echo "- OK: cancel event"
else
    echo "FAILED: event not found"
    exit 5
fi
if waitForEventAndSendMessage "other" 0.1 10; then
    echo "- OK: other event"
else
    echo "FAILED: event not found"
    exit 5
fi
echo "** Remark: next we want to have an error message"
if waitForEventAndSendMessage "not_existing" 0.1 10; then
    echo "FAILED: event was found"
    exit 5
else
    echo "- OK: unknown_event"
fi
