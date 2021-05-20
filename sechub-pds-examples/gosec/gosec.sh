#!/usr/bin/env sh

# PDS_JOB_WORKSPACE_LOCATION -> contains the `PDS-Job-UUID`
# `/output/result.txt` -> needs to be written
GO111MODULE=on gosec -fmt=sarif -out="$PDS_JOB_WORKSPACE_LOCATION/output/result.txt" "$PDS_JOB_WORKSPACE_LOCATION/upload/unzipped/sourcecode/"
