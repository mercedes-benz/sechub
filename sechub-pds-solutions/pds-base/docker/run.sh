#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

DEFAULT_PDS_MAX_FILE_UPLOAD_BYTES=52428800  # 50 MB
DEFAULT_PDS_HEARTBEAT_LOGGING="true"
SLEEP_TIME_IN_WAIT_LOOP="2h"

JAVA_DEBUG_OPTIONS=""
PID_JAVA_SERVER=""

###########################
# Trap and process signals
trap trigger_shutdown INT QUIT TERM

trigger_shutdown()
{
  if [ -n "$PID_JAVA_SERVER" ] ; then
    echo "`basename $0`: Caught shutdown signal! Sending SIGTERM to Java server process $PID_JAVA_SERVER"
    kill -TERM "$PID_JAVA_SERVER"
    # Wait until Java server process has ended
    wait "$PID_JAVA_SERVER"
  fi
  exit
}
###########################

wait_loop() {
    while true
    do
	    echo "wait_loop(): Sleeping for $SLEEP_TIME_IN_WAIT_LOOP."
	    sleep $SLEEP_TIME_IN_WAIT_LOOP
    done
}

start_server() {
    profiles="$1"

    check_setup

    database_options=""
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

        storage_options="-Dpds.storage.s3.endpoint=$S3_ENDPOINT"
        storage_options="$storage_options -Dpds.storage.s3.bucketname=$S3_BUCKETNAME"
        storage_options="$storage_options -Dpds.storage.s3.accesskey=$S3_ACCESSKEY"
        storage_options="$storage_options -Dpds.storage.s3.secretkey=$S3_SECRETKEY"

        echo "Object storage:"
        echo " * Endpoint: $S3_ENDPOINT"
        echo " * Bucketname: $S3_BUCKETNAME"
    else
        storage_options="-Dpds.storage.sharedvolume.upload.dir=$SHARED_VOLUME_UPLOAD_DIR"
    fi

    echo "Calling the run_additional.sh script"
    echo "---"
    /run_additional.sh
    echo "---"
    echo ""
    echo "Starting the SecHub PDS server"
    echo "PDS Version: $PDS_VERSION"

    # Regarding entropy collection:
    #   with JDK 8+ the "obscure workaround using file:///dev/urandom
    #   and file:/dev/./urandom is no longer required."
    #   (source: https://docs.oracle.com/javase/8/docs/technotes/guides/security/enhancements-8.html)
    java $JAVA_DEBUG_OPTIONS $database_options \
        $storage_options \
        -Dfile.encoding=UTF-8 \
        -Dspring.profiles.active="$profiles" \
        -Dpds.admin.userid="$ADMIN_USERID" \
        -Dpds.admin.apitoken="$ADMIN_APITOKEN" \
        -Dpds.techuser.userid="$TECHUSER_USERID" \
        -Dpds.techuser.apitoken="$TECHUSER_APITOKEN" \
        -Dpds.workspace.rootfolder="$WORKSPACE" \
        -Dpds.config.file=/pds/pds-config.json \
        -Dpds.upload.maximum.bytes="$PDS_MAX_FILE_UPLOAD_BYTES" \
        -Dpds.config.heartbeat.verbose.logging.enabled="$PDS_HEARTBEAT_LOGGING" \
        -Dserver.port=8444 \
        -Dserver.address=0.0.0.0 \
        -jar /pds/sechub-pds-*.jar &

    # Get process pid and wait until it ends
    #   The pid will be needed by function trigger_shutdown() in case we receive a termination signal.
    PID_JAVA_SERVER=$!
    wait "$PID_JAVA_SERVER"

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

if [ -z "$PDS_HEARTBEAT_LOGGING" ]
then
  export PDS_HEARTBEAT_LOGGING="$DEFAULT_PDS_HEARTBEAT_LOGGING"
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
    # Start with localserver settings
    start_server "pds_localserver"
else
    wait_loop
fi
