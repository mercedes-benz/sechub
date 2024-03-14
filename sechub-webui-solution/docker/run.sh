#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

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
    while true ; do
	    echo "wait_loop(): Sleeping for $SLEEP_TIME_IN_WAIT_LOOP."
	    sleep $SLEEP_TIME_IN_WAIT_LOOP
    done
}

setup_ssl() {
  if [ -n "${SECHUB_WEBUI_SSL_KEYSTORE_ALIAS}" -a "${SECHUB_WEBUI_SSL_KEYSTORE_ALIAS}" != "undefined" ] ; then
    # Create symlink to .p12 keystore file
    ln -s "$WEBUI_FOLDER/secrets/secret-ssl/keystore_file" ${SECHUB_WEBUI_SSL_KEYSTORE_LOCATION}
    cat - <<EOF
SSL server certificate:
- alias: $SECHUB_WEBUI_SSL_KEYSTORE_ALIAS
- location: $SECHUB_WEBUI_SSL_KEYSTORE_LOCATION
EOF
  fi
}

start_server() {

    check_setup

    setup_ssl

    echo
    echo "Starting the SecHub WebUI"
    echo "WebUI Version: $WEBUI_VERSION"
    echo "WebUI Spring Server Profiles: \"$SPRING_PROFILES_ACTIVE\""
    echo "SecHub Server URL: $WEBUI_SECHUB_SERVER_URL"
    echo "SecHub Server UserID: $WEBUI_SECHUB_USERID"

    java $JAVA_DEBUG_OPTIONS \
        -Dfile.encoding=UTF-8 \
        -Dserver.port=4443 \
        -Dserver.address=0.0.0.0 \
        -jar $WEBUI_FOLDER/sechub-webui-*.jar &

    # Get process pid and wait until it ends
    #   The pid will be needed by function trigger_shutdown() in case we receive a termination signal.
    PID_JAVA_SERVER=$!
    wait "$PID_JAVA_SERVER"

    keep_container_alive_or_exit
}

keep_container_alive_or_exit() {
    if [ "$KEEP_CONTAINER_ALIVE_AFTER_WEBUI_CRASHED" = "true" ] ; then
        echo "[ERROR] WEB UI crashed, but keeping the container alive."
        wait_loop
    fi
}

check_setup () {
    # Verify that mandatory vars are set
    check_variable "$SECHUB_USERID" "SECHUB_USERID"
    check_variable "$SECHUB_APITOKEN" "SECHUB_APITOKEN"
    check_variable "$SECHUB_SERVER_URL" "SECHUB_SERVER_URL"
    check_variable "$SPRING_PROFILES_ACTIVE" "SPRING_PROFILES_ACTIVE"

    # Set SecHub server properties for Spring Boot as env vars
    # (don't reveal confidential data in process list)
    export WEBUI_SECHUB_SERVER_URL="$SECHUB_SERVER_URL"
    export WEBUI_SECHUB_USERID="$SECHUB_USERID"
    export WEBUI_SECHUB_APITOKEN="$SECHUB_APITOKEN"
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

if [ "$JAVA_ENABLE_DEBUG" = "true" ] ; then
    # By using `address=*:15025` the server will bind
    # all available IP addresses to port 15025
    # otherwise the container cannot be accessed from outside
    JAVA_DEBUG_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,address=*:15025"
fi

if [ "$WEBUI_START_MODE" = "server" ] ; then
    start_server
elif [ "$WEBUI_START_MODE" = "development" ] ; then
    wait_loop
else
    echo "Unknown start mode: \"$WEBUI_START_MODE\". Exiting."
    exit 1
fi
