#!/usr/bin/env bash

# GoSec needs access to the go binary
export PATH="$PATH:/usr/local/go/bin"
GO111MODULE=on gosec -fmt=sarif -out="$PDS_JOB_RESULT_FILE" "$PDS_JOB_SOURCECODE_UNZIPPED_FOLDER/..."
