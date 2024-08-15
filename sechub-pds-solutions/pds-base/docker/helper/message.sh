#!/bin/bash
# SPDX-License-Identifier: MIT

function writeUniqueMessageFile() {
      MSG_PREFIX="$1"
      MESSAGE="$2"

      MESSAGE_FILE_PATH="${PDS_JOB_USER_MESSAGES_FOLDER}/${MSG_PREFIX}_message_$(date +%Y-%m-%d_%H.%M.%S_%N).txt"
      echo "$MESSAGE" > "$MESSAGE_FILE_PATH"

      # additionally echo the message to make it part of the output stream:
      echo "$MSG_PREFIX: $MESSAGE"
}

function infoMessage() {
    writeUniqueMessageFile "INFO" "$1"
}

function warnMessage() {
    writeUniqueMessageFile "WARNING" "$1"
}

function errorMessage() {
    writeUniqueMessageFile "ERROR" "$1"
}