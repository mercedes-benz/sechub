#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

. "$SCRIPT_FOLDER/common.sh"

echo "Look for potential .git folder to perform history scan."

git_directory=$(find "$PDS_JOB_EXTRACTED_SOURCES_FOLDER" -type d -name ".git")
repository_root_directory=$(dirname "$git_directory")

scan_target_directory="$PDS_JOB_EXTRACTED_SOURCES_FOLDER"

# It is important to specify the target source folder with the current directory ".", because gitleaks puts this path in the report.
# The full path containing "$PDS_JOB_EXTRACTED_SOURCES_FOLDER" is not useful in the report.
gitleaks_options="--log-level debug --config $TOOL_FOLDER/custom-gitleaks.toml --source . --report-format sarif --report-path $PDS_JOB_RESULT_FILE --exit-code 0"

# If the history scan was disabled, a normal filesystem scan is performed.
if [ "$GITLEAKS_HISTORY_SCAN_ENABLED" = "false" ]
then
    gitleaks_options="$gitleaks_options --no-git"
    echo "History scan disabled. Perform secret scan on filesystem without history deepscan." | tee "$PDS_JOB_USER_MESSAGES_FOLDER"/history-scan-disabled.txt
    
# If no '.git' directory was found we cannot scan the git history
elif [ -z "$git_directory" ]
then
    gitleaks_options="$gitleaks_options --no-git"
    echo "No .git folder was found. Perform secret scan on filesystem without history deepscan." | tee "$PDS_JOB_USER_MESSAGES_FOLDER"/no-git.txt

# If the value of 'git_directory' is not a valid directory there is more than a single result of the find command
elif [ ! -d "$git_directory" ]
then
    gitleaks_options="$gitleaks_options --no-git"
    echo "Multiple .git folders were found. Perform secret scan on filesystem without history deepscan." | tee "$PDS_JOB_USER_MESSAGES_FOLDER"/multiple-git.txt

# If exactly one '.git' directory was found we scan the git history
else
    scan_target_directory="$repository_root_directory"
    echo ".git folder was found. Perform secret scan with history deepscan." | tee "$PDS_JOB_USER_MESSAGES_FOLDER"/history-scan.txt
fi

echo "Running Gitleaks"
cd "$scan_target_directory"
"$TOOL_FOLDER"/gitleaks detect $gitleaks_options
