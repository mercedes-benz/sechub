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
            "name": "pds",
            "credentials": [
                {
                    "accessKey": "${S3_ACCESSKEY}",
                    "secretKey": "${S3_SECRETKEY}"
                }
            ],
            "actions": [
                "Read:${S3_BUCKETNAME}",
                "Write:${S3_BUCKETNAME}",
                "List:${S3_BUCKETNAME}"
            ]
        }
    ]
}
JSON
)


    echo "$s3_config" > "/home/$STORAGE_USER/s3_config.json"
}

server () {
    create_s3_config

    echo "Start SeaweedFS"

    weed server -dir=/storage -master.volumeSizeLimitMB=1024 -master.volumePreallocate=false -s3 -s3.port=9000 -s3.config="/home/$STORAGE_USER/s3_config.json" &
    weed_server_process_id=$!

    init

    wait $weed_server_process_id
}

init() {
    sleep 25

    # Create bucket
    echo "Create bucket"
    echo "s3.bucket.create -name $S3_BUCKETNAME" | weed shell
}

if [ "$OBJECT_STORAGE_START_MODE" = "server" ]
then
    server
else
    debug
fi