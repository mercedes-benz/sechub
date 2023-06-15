#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# GoSec needs access to the go binary
export PATH="$TOOL_FOLDER/gosec:/usr/local/go/bin:$PATH"
gosec -fmt=sarif -out="$PDS_JOB_RESULT_FILE" "$PDS_JOB_EXTRACTED_SOURCES_FOLDER/..."

# GoSec returns an exit code of 1 in case of findings
exit 0
