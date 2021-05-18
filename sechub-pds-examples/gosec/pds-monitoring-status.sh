#!/usr/bin/env bash

curl -X GET --silent --insecure curl -u "$PDS_USERID:$PDS_APITOKEN" --header "Accept: application/json" $PDS_SERVER/api/admin/monitoring/status
