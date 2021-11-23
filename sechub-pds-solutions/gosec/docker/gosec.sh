#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# GoSec needs access to the go binary
export PATH="$TOOL_FOLDER/gosec:/usr/local/go/bin:$PATH"
GO111MODULE=on gosec -fmt=sarif -out="$PDS_JOB_RESULT_FILE" "$PDS_JOB_SOURCECODE_UNZIPPED_FOLDER/..."

# GoSec returns an exit code of 1 in case findings are found
exit 0