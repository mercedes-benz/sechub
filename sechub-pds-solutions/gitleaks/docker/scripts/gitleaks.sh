#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

. "$SCRIPT_FOLDER/common.sh"

echo "Running Gitleaks"
cd "$PDS_JOB_EXTRACTED_SOURCES_FOLDER"
"$TOOL_FOLDER"/gitleaks detect --log-level debug --config "$TOOL_FOLDER"/custom-gitleaks.toml --no-git --source . --report-format sarif --report-path "$PDS_JOB_RESULT_FILE" --exit-code 0
