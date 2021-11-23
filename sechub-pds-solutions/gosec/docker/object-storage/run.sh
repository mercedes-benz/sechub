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
            "name": "user",
            "credentials": [
                {
                "accessKey": "${S3_ACCESSKEY}",
                "secretKey": "${S3_SECRETKEY}"
                }
            ],
            "actions": [
                "Read:${S3_BUCKETNAME}",
                "Write:${S3_BUCKETNAME}"
            ]
        }
    ]
}
JSON
)

    s3_config2=$(cat <<-JSON
{
    "identities": [
        {
            "name": "me",
            "credentials": [
                {
                "accessKey": "any",
                "secretKey": "anylongkey"
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


    echo "$s3_config2" > "/home/$USER/s3_config.json"
}

server () {
    create_s3_config

    echo "Start SeaweedFS"

    weed server -dir=/storage -master.volumeSizeLimitMB=1024 -master.volumePreallocate=false -s3 -s3.port=9000 -s3.config="/home/$USER/s3_config.json"
}

if [ "$OBJECT_STORAGE_START_MODE" = "server" ]
then
    server
else
    debug
fi
