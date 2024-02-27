#!/bin/bash 
# SPDX-License-Identifier: MIT

# Append content from each uploaded file to result file - thosee files do contain info/error messages
# for the test and will be merged into one result file which is interpreted by integration tests.
function mergeFolderFilesRecursivelyIntoResultFile(){
    READING_TYPE=$1
    FOLDER_TO_READ_AND_MERGE=$2
    RESULT_FILE=$3
    DEBUG=$4
   
    debug "> reading $READING_TYPE from: ${FOLDER_TO_READ_AND_MERGE}" $DEBUG
    
    find "${FOLDER_TO_READ_AND_MERGE}" -type f | 
    while read src
     do  cat "${src}" >> "${RESULT_FILE}"
     debug "> appended '$src' to ${RESULT_FILE}" $DEBUG
    done
}
