#!/usr/bin/env bash

jobUUID=$1

curl -X PUT --insecure curl -u "$PDS_USER:$PDS_PASSWORD" --header "Accept: application/json" $PDS_HOST/api/job/$jobUUID/mark-ready-to-start
