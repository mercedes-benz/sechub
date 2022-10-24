#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

function log_step() {
    local message="$1"
    printf '\n>> %s\n' "$message"
}

if [[ "$PDS_JOB_HAS_EXTRACTED_BINARIES" == "true" ]]
then
    log_step "Has extracted binaries"
else
    echo ""
    log_step "ERROR: Not a binary upload."
    echo ""
    echo "Workspace location structure:"
    echo ""
    tree "$PDS_JOB_WORKSPACE_LOCATION"
    exit 1
fi

log_step "Workspace location structure:"
tree "$PDS_JOB_EXTRACTED_BINARIES_FOLDER"

log_step "Searching for .tar files"
tar_files=$(cd "$PDS_JOB_EXTRACTED_BINARIES_FOLDER" && find . -type f -name "*.tar" | sed 's|^./||')

if [[ -z "$tar_files" ]]
then
    echo "ERROR: Found no tar file to analyze."
    exit 2
fi

number_of_tar_files=$( echo "$tar_files" | wc -l )
if [[ "$number_of_tar_files" -eq 1 ]]
then
    echo "Found one tar file to analyze."
else
    echo "WARNING: Found more than one tar file."
    echo "Total number of tar files: $number_of_tar_files"
fi

tar_file=$( echo "$tar_files" | head -n 1 )
tar_file_path="$PDS_JOB_EXTRACTED_BINARIES_FOLDER/$tar_file"

if [[ ! -s "$tar_file_path" ]]
then
    echo "ERROR: File is empty."
    exit 3
fi

log_step "Starting Tern"
echo "Analyzing: $tar_file"
echo "Path: $tar_file_path"

tern report -f spdxjson -w "$tar_file_path" -o "$PDS_JOB_RESULT_FILE"

# analyzing container from repository
#tern report -f spdxjson -i mariadb:latest -o "$PDS_JOB_RESULT_FILE"
