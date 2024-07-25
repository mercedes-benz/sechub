#!/bin/sh
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

  if [ "$POSTGRES_ENABLED" = true ] ; then
    echo "Using PostgreSQL database connection:"
    echo " * URL: $DATABASE_CONNECTION"
    echo " * Username: $DATABASE_USERNAME"

    profiles="$profiles,pds_postgres"
    # Set variables for Java Spring app:
    export SPRING_DATASOURCE_URL="$DATABASE_CONNECTION"
    export SPRING_DATASOURCE_USERNAME="$DATABASE_USERNAME"
    export SPRING_DATASOURCE_PASSWORD="$DATABASE_PASSWORD"
  fi

  if [ "$S3_ENABLED" = true ] ; then
    echo "Using S3 object storage:"
    echo " * Endpoint: $S3_ENDPOINT"
    echo " * Bucketname: $S3_BUCKETNAME"

    # Set variables for Java Spring app:
    export PDS_STORAGE_S3_ENDPOINT="$S3_ENDPOINT"
    export PDS_STORAGE_S3_BUCKETNAME="$S3_BUCKETNAME"
    export PDS_STORAGE_S3_ACCESSKEY="$S3_ACCESSKEY"
    export PDS_STORAGE_S3_SECRETKEY="$S3_SECRETKEY"
  else
    echo "Using shared upload directory: $SHARED_VOLUME_UPLOAD_DIR"
    # Set variable for Java Spring app:
    export PDS_STORAGE_SHAREDVOLUME_UPLOAD_DIR="$SHARED_VOLUME_UPLOAD_DIR"
  fi

  echo "Calling the run_additional.sh script"
  echo "---"
  /run_additional.sh
  echo "---"
  echo ""
  echo "Starting the SecHub PDS server"
  echo "PDS Version: $PDS_VERSION"

  # Set variables for Java Spring app:
  export SPRING_PROFILES_ACTIVE="$profiles"
  export PDS_ADMIN_USERID="$ADMIN_USERID"
  export PDS_ADMIN_APITOKEN="$ADMIN_APITOKEN"
  export PDS_TECHUSER_USERID="$TECHUSER_USERID"
  export PDS_TECHUSER_APITOKEN="$TECHUSER_APITOKEN"
  export PDS_WORKSPACE_ROOTFOLDER="$WORKSPACE"
  export PDS_CONFIG_FILE="$PDS_FOLDER/pds-config.json"
  export PDS_UPLOAD_MAXIMUM_BYTES="$PDS_MAX_FILE_UPLOAD_BYTES"
  export PDS_CONFIG_HEARTBEAT_VERBOSE_LOGGING_ENABLED="$PDS_HEARTBEAT_LOGGING"

  # Start Java Spring app:
  java $JAVA_DEBUG_OPTIONS \
    -Dfile.encoding=UTF-8 \
    -Dserver.port=8444 \
    -Dserver.address=0.0.0.0 \
    -jar "$PDS_FOLDER/"sechub-pds-*.jar &

  # Get process pid and wait until it ends
  #   The pid will be needed by function trigger_shutdown() in case we receive a termination signal.
  PID_JAVA_SERVER=$!
  wait "$PID_JAVA_SERVER"

  keep_container_alive_or_exit
}

keep_container_alive_or_exit() {
  if [ "$KEEP_CONTAINER_ALIVE_AFTER_PDS_CRASHED" = "true" ] ; then
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

  if [ -z "$value" ] ; then
    echo "Environment variable $name not set."
    exit 1
  fi
}

##################
# main

if [ -z "$PDS_MAX_FILE_UPLOAD_BYTES" ] ; then
  export PDS_MAX_FILE_UPLOAD_BYTES="$DEFAULT_PDS_MAX_FILE_UPLOAD_BYTES"
fi

if [ -z "$PDS_HEARTBEAT_LOGGING" ] ; then
  export PDS_HEARTBEAT_LOGGING="$DEFAULT_PDS_HEARTBEAT_LOGGING"
fi

if [ "$JAVA_ENABLE_DEBUG" = "true" ] ; then
  # By using `address=*:15024` the server will bind
  # all available IP addresses to port 15024
  # otherwise the container cannot be accessed from outside
  JAVA_DEBUG_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,address=*:15024"
fi

if [ "$PDS_START_MODE" = "localserver" ] ; then
  # Start with localserver settings
  start_server "pds_localserver"
else
  wait_loop
fi
