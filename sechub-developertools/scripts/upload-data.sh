#!/usr/bin/env bash

jobUUID=$1

echo "Job UUID: $jobUUID"


# the file needs to be called sourcecode.zip
curl -vs -X POST --insecure -u "$PDS_USERID:$PDS_APITOKEN" -H "Content-Type: multipart/form-data" -F "file=@sourcecode.zip" -F "checkSum=77b5d502ab18ab4ca4e4bfd237f8e57b256e4e8db93d0e1f0604a546e63cd682"  --trace-ascii "trace.txt" "$PDS_SERVER/api/job/${jobUUID}/upload/sourcecode.zip"

