#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

flawfinder --sarif "$PDS_JOB_SOURCECODE_UNZIPPED_FOLDER/" > "$PDS_JOB_RESULT_FILE"
