#!/bin/bash
# SPDX-License-Identifier: MIT
# You can paste this snippet inside your launcher script and use it to handle messages

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
