#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

debug () {
    while true
    do
	    echo "Press [CTRL+C] to stop.."
	    sleep 120
    done
}

server () {
    echo "Start MinIO"

    # start MinIO server in background
    minio server /storage &
    minio_server_process_id=$!

    # setup MinIO
    setup

    # wait for MinIO server process
    wait $minio_server_process_id
}

setup () {
    # wait for 3 seconds until the server is available
    sleep 3

    echo "Setup MinIO object storage"

    localhost="http://127.0.0.1:9000"

    # set root user connection information
    mcli alias set minio_admin "$localhost" "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD"

    # show server state
    mcli admin info minio_admin

    # add new user as root user
    mcli admin user add minio_admin "$S3_ACCESSKEY" "$S3_SECRETKEY"

    # create bucket as user
    mcli mb minio_admin/"$S3_BUCKETNAME" 

    # set readwrite policy of bucket for minio_user
    mcli admin policy set minio_admin/"$S3_BUCKETNAME" readwrite user="$S3_ACCESSKEY"
}

if [ "$OBJECT_STORAGE_START_MODE" = "server" ]
then
    server
else
    debug
fi
