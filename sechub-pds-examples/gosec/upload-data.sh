#!/usr/bin/env bash

jobUUID=$1

curl -X POST --silent --insecure -u "$PDS_USERID:$PDS_APITOKEN" $PDS_SERVER/api/job/${jobUUID}/upload \
--header "Content-Type: multipart/form-data;charset=UTF-8" \
--form 'file=@test.zip' \
--form 'checkSum=bd76743bc6db03d74fc6e78331fd52ece5ccd7c96bd8692295166177df2082f9'
