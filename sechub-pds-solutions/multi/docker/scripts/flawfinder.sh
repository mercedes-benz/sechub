#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

cd "$PDS_JOB_EXTRACTED_SOURCES_FOLDER/"
flawfinder --sarif . > "$PDS_JOB_RESULT_FILE"

# remove "./ in front of uri locations
# workaround for https://github.com/david-a-wheeler/flawfinder/issues/67
# use | character as separator
sed --in-place 's|"uri": "./|"uri": "|g' "$PDS_JOB_RESULT_FILE"
