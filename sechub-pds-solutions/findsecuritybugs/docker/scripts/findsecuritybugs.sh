#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

$TOOL_FOLDER/findsecbugs.sh -effort:max -progress -sarif -output "$PDS_JOB_RESULT_FILE" "$PDS_JOB_SOURCECODE_UNZIPPED_FOLDER/"
