#!/bin/bash
# SPDX-License-Identifier: MIT
function debug(){
     MESSAGE=$1
     DEBUG=$2
     if [[ "$DEBUG" = "true" ]]; then
        echo "DEBUG:$MESSAGE"
     fi
} 
# Append content from each uploaded file to result file - thosee files do contain info/error messages
# for the test and will be merged into one result file which is interpreted by integration tests.
function mergeFolderFilesRecursivelyIntoResultFile(){
    READING_TYPE=$1
    FOLDER_TO_READ_AND_MERGE=$2
    RESULT_FILE=$3
    DEBUG=$4
   
    debug "> reading $READING_TYPE from: ${FOLDER_TO_READ_AND_MERGE}" $DEBUG
    
    find ${FOLDER_TO_READ_AND_MERGE} -type f | 
    while read src
     do  cat "${src}" >> ${RESULT_FILE}
     debug "> appended '$src' to ${RESULT_FILE}" $DEBUG
    done
}

function errEcho () {
    echo "$@" >&2
}

# ----------------------------------------------------------
# Helper methods for producing user messages
# ----------------------------------------------------------

function writeUniqueMessageFile(){
      MSG_PREFIX=$1
      MESSAGE=$2

      MESSAGE_FILE_PATH="${PDS_JOB_USER_MESSAGES_FOLDER}/${MSG_PREFIX}_message_$(date +%Y-%m-%d_%H.%M.%S_%N).txt"
      echo "$MESSAGE" > "$MESSAGE_FILE_PATH"
      
      # additional echo to have the message also in output stream available:
      echo "${MSG_PREFIX} message: $MESSAGE"
}

function infoMessage(){
    writeUniqueMessageFile "INFO" "$1"
}

function warnMessage(){
    writeUniqueMessageFile "WARNING" "$1"
}

function errorMessage(){
    writeUniqueMessageFile "ERROR" "$1"
}

# Usage:
# 
# ----
# infoMessage "this is an info message"
# warnMessage "this is a warning message"
# errorMessage "this is an error message
#     with multiple lines... 
# "
# ----
# 
# The created message file names from the example above look like this: 
# 
# $job_folder_workspace/output/messages
#├── ERROR_message_2022-06-24_17.56.52_822554054.txt
#├── INFO_message_2022-06-24_17.56.52_818872869.txt
#└── WARNING_message_2022-06-24_17.56.52_820825342.txt

