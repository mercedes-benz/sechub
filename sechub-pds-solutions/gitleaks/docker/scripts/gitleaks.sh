#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

if [ "$PDS_JOB_HAS_EXTRACTED_SOURCES" = "true" ]
then
    echo "Folder structure:"
    echo ""
    tree "$PDS_JOB_EXTRACTED_SOURCES_FOLDER"

    echo "Running Gitleaks"
    cd "$PDS_JOB_EXTRACTED_SOURCES_FOLDER"
    "$TOOL_FOLDER"/gitleaks detect --log-level debug --no-git --source . --report-format sarif --report-path "$PDS_JOB_RESULT_FILE" --exit-code 0
else
    echo "No extracted source code found"
    exit 1
fi