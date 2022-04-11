#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

echo "# Extracted files"
ls "$PDS_JOB_SOURCECODE_UNZIPPED_FOLDER/"

echo "# Start analyzing"
"$TOOL_FOLDER/findsecbugs.sh" -sarif -output "$PDS_JOB_RESULT_FILE" -progress "$PDS_JOB_SOURCECODE_UNZIPPED_FOLDER/"