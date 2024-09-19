#!/bin/bash
# SPDX-License-Identifier: MIT

extracted_folder=""
if [[ "$PDS_JOB_HAS_EXTRACTED_SOURCES" == "true" ]]
then
    echo "Extracted sources"
    extracted_folder="$PDS_JOB_EXTRACTED_SOURCES_FOLDER"
else
    echo ""
    echo "ERROR: Unrecognized file type. No source code."
    exit 1
fi

cloc "$extracted_folder" --json --out="$PDS_JOB_RESULT_FILE"
