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
    while true
    do
	    echo "wait_loop(): Sleeping for $SLEEP_TIME_IN_WAIT_LOOP."
	    sleep $SLEEP_TIME_IN_WAIT_LOOP
    done
}

start_server() {
    profiles="$1"

    check_setup

    echo ""
    echo "Starting the WebUI"
    echo "WebUI Version: $WEBUI_VERSION"
    echo "SecHub Server URL: $SECHUB_SERVER_URL"

    # Regarding entropy collection:
    #   with JDK 8+ the "obscure workaround using file:///dev/urandom
    #   and file:/dev/./urandom is no longer required."
    #   (source: https://docs.oracle.com/javase/8/docs/technotes/guides/security/enhancements-8.html)
    java $JAVA_DEBUG_OPTIONS \
        -Dfile.encoding=UTF-8 \
        -Dspring.profiles.active="$profiles" \
        -Dwebui.sechub.apitoken="$SECHUB_APITOKEN" \
        -Dwebui.sechub.userid="$SECHUB_USERID" \
        -Dwebui.sechub.server-url="$SECHUB_SERVER_URL" \
        -Dserver.port=4443 \
        -Dserver.address=0.0.0.0 \
        -jar /webui/sechub-webui-*.jar &

    # Get process pid and wait until it ends
    #   The pid will be needed by function trigger_shutdown() in case we receive a termination signal.
    PID_JAVA_SERVER=$!
    wait "$PID_JAVA_SERVER"

    keep_container_alive_or_exit
}

keep_container_alive_or_exit() {
    if [ "$KEEP_CONTAINER_ALIVE_AFTER_WEBUI_CRASHED" = "true" ]
    then
        echo "[ERROR] WEB UI crashed, but keeping the container alive."
        wait_loop
    fi
}

check_setup () {
    check_variable "$SECHUB_USERID" "SECHUB_USERID"
    check_variable "$SECHUB_APITOKEN" "SECHUB_APITOKEN"
    check_variable "$SECHUB_SERVER_URL" "SECHUB_SERVER_URL"
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

if [ "$JAVA_ENABLE_DEBUG" = "true" ]
then
    # By using `address=*:15025` the server will bind
    # all available IP addresses to port 15025
    # otherwise the container cannot be accessed from outside
    JAVA_DEBUG_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,address=*:15025"
fi

if [ "$WEBUI_START_MODE" = "localserver" ]
then
    start_server "webui_localserver"
elif [ "$WEBUI_START_MODE" = "mocked" ]
then
    start_server "webui_localserver,webui_mocked"
else
    wait_loop
fi
