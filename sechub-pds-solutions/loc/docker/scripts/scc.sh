#!/bin/bash
# SPDX-License-Identifier: MIT

echo "Running PDS Scc"

extracted_folder=""
if [[ "$PDS_JOB_HAS_EXTRACTED_SOURCES" == "true" ]]
then
    echo "Found extracted sources"
    extracted_folder="$PDS_JOB_EXTRACTED_SOURCES_FOLDER"
else
    echo ""
    echo "ERROR: No source code."
    exit 1
fi

scc -f cloc-yaml "$extracted_folder" > scc.yaml && \
cat scc.yaml | sed 's/  version:/  cloc_version:/' | yq > "$PDS_JOB_RESULT_FILE"
