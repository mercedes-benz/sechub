#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

DEFAULT_PDS_MAX_FILE_UPLOAD_BYTES=52428800  # 50 MB

# the . dot command is like the bash buid-in command `source`
. "/run_additional.sh"

JAVA_DEBUG_OPTIONS=""

wait_loop() {
    while true
    do
	    echo "Press [CTRL+C] to stop.."
	    sleep 120
    done
}

debug() {
    wait_loop
}

# Start with localserver settings 
localserver () {
    start_server "pds_localserver"
}

start_server() {
    profiles="$1"

    check_setup

    database_options=""
    storage_options="-Dsechub.pds.storage.sharedvolume.upload.dir=$SHARED_VOLUME_UPLOAD_DIR"

    if [ "$POSTGRES_ENABLED" = true ]
    then
        echo "Using database: Postgres"

        profiles="$profiles,pds_postgres"
        database_options="-Dspring.datasource.url=$DATABASE_CONNECTION -Dspring.datasource.username=$DATABASE_USERNAME  -Dspring.datasource.password=$DATABASE_PASSWORD"

        echo "Database connection:"
        echo " * URL: $DATABASE_CONNECTION"
        echo " * Username: $DATABASE_USERNAME"
    fi

    if [ "$S3_ENABLED" = true ]
    then
        echo "Using object storage"

        storage_options="-Dsechub.pds.storage.s3.endpoint=$S3_ENDPOINT"
        storage_options="$storage_options -Dsechub.pds.storage.s3.bucketname=$S3_BUCKETNAME"
        storage_options="$storage_options -Dsechub.pds.storage.s3.accesskey=$S3_ACCESSKEY"
        storage_options="$storage_options -Dsechub.pds.storage.s3.secretkey=$S3_SECRETKEY"

        echo "Object storage:"
        echo " * Endpoint: $S3_ENDPOINT"
        echo " * Bucketname: $S3_BUCKETNAME"
    fi

    # call the additional function in the run_additional.sh script
    run_additional

    # Regarding entropy collection:
    #   with JDK 8+ the "obscure workaround using file:///dev/urandom 
    #   and file:/dev/./urandom is no longer required."
    #   (source: https://docs.oracle.com/javase/8/docs/technotes/guides/security/enhancements-8.html)
    java $JAVA_DEBUG_OPTIONS $database_options \
        $storage_options \
        -Dfile.encoding=UTF-8 \
        -Dspring.profiles.active="$profiles" \
        -DsecHub.pds.admin.userid="$ADMIN_USERID" \
        -Dsechub.pds.admin.apitoken="$ADMIN_APITOKEN" \
        -DsecHub.pds.techuser.userid="$TECHUSER_USERID" \
        -Dsechub.pds.techuser.apitoken="$TECHUSER_APITOKEN" \
        -Dsechub.pds.workspace.rootfolder=/workspace \
        -Dsechub.pds.config.file=/pds/pds-config.json \
        -Dpds.upload.maximum.bytes="$PDS_MAX_FILE_UPLOAD_BYTES" \
        -Dserver.port=8444 \
        -Dserver.address=0.0.0.0 \
        -jar /pds/sechub-pds-*.jar
    
    keep_container_alive_or_exit
}

keep_container_alive_or_exit() {
    if [ "$KEEP_CONTAINER_ALIVE_AFTER_PDS_CRASHED" = "true" ]
    then
        echo "[ERROR] PDS crashed, but keeping the container alive."
        wait_loop
    fi
}

check_setup () {
    check_variable "$ADMIN_USERID" "ADMIN_USERID"
    check_variable "$ADMIN_APITOKEN" "ADMIN_APITOKEN"
    check_variable "$TECHUSER_USERID" "TECHUSER_USERID"
    check_variable "$TECHUSER_APITOKEN" "TECHUSER_APITOKEN"
    check_variable "$SHARED_VOLUME_UPLOAD_DIR" "SHARED_VOLUME_UPLOAD_DIR"
    check_variable "$PDS_MAX_FILE_UPLOAD_BYTES" "PDS_MAX_FILE_UPLOAD_BYTES"
}

check_variable () {
    value="$1"
    name="$2"

    if [ -z "$value" ]
    then
        echo "Environment variable $name not set."
        exit 1
    fi
}

##################
# main

if [ -z "$PDS_MAX_FILE_UPLOAD_BYTES" ]
then
  export PDS_MAX_FILE_UPLOAD_BYTES="$DEFAULT_PDS_MAX_FILE_UPLOAD_BYTES"
fi

if [ "$JAVA_ENABLE_DEBUG" = "true" ]
then
    # By using `address=*:15024` the server will bind 
    # all available IP addresses to port 15024
    # otherwise the container cannot be accessed from outside
    JAVA_DEBUG_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,address=*:15024"
fi

if [ "$PDS_START_MODE" = "localserver" ]
then
    localserver
else
    debug
fi
