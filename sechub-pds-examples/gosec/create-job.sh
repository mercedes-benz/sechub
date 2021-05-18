#!/usr/bin/env bash

curl -X POST --silent --insecure -u "$PDS_USERID:$PDS_APITOKEN" --header "Content-Type: application/json" -d "@job.json" $PDS_SERVER/api/job/create
