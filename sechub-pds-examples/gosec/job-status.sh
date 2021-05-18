#!/usr/bin/env bash

jobUUID=$1

curl -X GET --silent --insecure curl -u "$PDS_USER:$PDS_PASSWORD" --header "Accept: application/json" $PDS_HOST/api/job/$jobUUID/status
