#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

scan_results_folder="$PDS_JOB_WORKSPACE_LOCATION/results"

echo ""
echo "----------"
echo "Kics Setup"
echo "----------"
echo ""

if [ "$PDS_JOB_HAS_EXTRACTED_SOURCES" = "true" ]
then
    echo "Found sources to scan."
else
    echo ""
    echo "ERROR: No sources found."
    echo ""
    echo "Workspace location structure:"
    echo ""
    tree "$PDS_JOB_WORKSPACE_LOCATION"
    exit 1
fi

echo ""
echo "-------------"
echo "Starting scan"
echo "-------------"
echo ""

echo "Starting Kics"

kics scan --ci --exclude-categories "Best practices" --disable-full-descriptions --report-formats "sarif" --output-path "$scan_results_folder" --path "$PDS_JOB_SOURCECODE_UNZIPPED_FOLDER/"

sleep 60

echo "Copy result file"
echo "Results folder: $scan_results_folder"
tree "$scan_results_folder"

cp "$scan_results_folder/results.sarif" "$PDS_JOB_RESULT_FILE"