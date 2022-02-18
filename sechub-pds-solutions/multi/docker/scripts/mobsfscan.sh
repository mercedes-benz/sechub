#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

cd "$PDS_JOB_SOURCECODE_UNZIPPED_FOLDER/"
mobsfscan --sarif --output "$PDS_JOB_RESULT_FILE" "."
