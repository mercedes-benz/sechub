#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

cd "$PDS_JOB_SOURCECODE_UNZIPPED_FOLDER/"
flawfinder --sarif . > "$PDS_JOB_RESULT_FILE"
