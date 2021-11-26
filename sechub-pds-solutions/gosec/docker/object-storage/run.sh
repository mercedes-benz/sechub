#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

debug () {
    create_s3_config

    while true
    do
	    echo "Press [CTRL+C] to stop.."
	    sleep 120
    done
}

create_s3_config() {
    s3_config=$(cat <<-JSON
{
    "identities": [
        {
            "name": "admin",
            "credentials": [
                {
                "accessKey": "${S3_ACCESSKEY}",
                "secretKey": "${S3_SECRETKEY}"
                }
            ],
            "actions": [
                "Read",
                "Write",
                "List",
                "Tagging",
                "Admin"
            ]
        }
    ]
}
JSON
)


    echo "$s3_config" > "/home/$USER/s3_config.json"
}

server () {
    create_s3_config

    echo "Start SeaweedFS"

    weed server -dir=/storage -master.volumeSizeLimitMB=1024 -master.volumePreallocate=false -s3 -s3.port=9000 -s3.config="/home/$USER/s3_config.json" &
    weed_server_process_id=$!

    init

    wait $weed_server_process_id
}

init() {
    sleep 25
    
    echo "# init"

    export AWS_ACCESS_KEY_ID=${S3_ACCESSKEY}
    export AWS_SECRET_ACCESS_KEY=${S3_SECRETKEY}

s3cmd_config=$(cat <<-CONFIG
# Setup endpoint
host_base = localhost:9000
host_bucket = localhost:9000
use_https = No
# Enable S3 v4 signature APIs
signature_v2 = False
CONFIG
)
    echo "$s3cmd_config" > "/home/$USER/.s3cfg"
    s3cmd mb s3://pds
}

if [ "$OBJECT_STORAGE_START_MODE" = "server" ]
then
    server
else
    debug
fi