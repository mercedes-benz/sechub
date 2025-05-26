#!/bin/bash
# SPDX-License-Identifier: MIT

LOGFILE="integrationtest-console.log"

# --------------------------------------------------
#  Start / Stop script for integration test server
# --------------------------------------------------
cd `dirname $0`

SECHUB_DEFAULT_PORT=8443
SECHUB_DEFAULT_VERSION="0.0.0"
SECHUB_DEFAULT_TEMPFOLDER="temp-shared"
MANAGEMENT_SERVER_PORT_DEFAULT=20250

function log {
    echo "$1"
    echo "`date +%Y-%m-%d\ %H:%M:%S` $1" >> "$LOGFILE"
}

function usage {
    log "usage: `basename $0` start [<serverVersion>] [<serverPort>] [<sharedTempSharedVolumeFolder>]"
    log "       `basename $0` stop|status|waitForStop|waitForAlive [<serverPort>]"
    log "       Control local SecHub server for e.g. integrationtests"
    log "       Defaults:"
    log "       - server version: \"$SECHUB_DEFAULT_VERSION\""
    log "       - server port: $SECHUB_DEFAULT_PORT"
    log "       - temp shared volume folder: \"$SECHUB_DEFAULT_TEMPFOLDER\""
    log "       - Spring management port: $MANAGEMENT_SERVER_PORT_DEFAULT (can be overridden with env var MANAGEMENT_SERVER_PORT)"
}

function isAlive {
    unset SECHUB_ITS_ALIVE_HTTP_STATUS
    log "Checking alive state"
    SECHUB_ITS_ALIVE_HTTP_STATUS=$(curl -k -o /dev/null -I -L -s -w "%{http_code}" https://localhost:$SERVER_PORT/api/anonymous/integrationtest/alive)
    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        log "-> Integration test server is running"
        return 0
    elif [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 401 ]; then
        log "-> Integration test server REST not correct implemented - no anonymous access possible! Fix this!"
        exit 666
    else
        log "-> Integration test server is not responding (state = $SECHUB_ITS_ALIVE_HTTP_STATUS)"
    fi
    # Server is not running
    return 1
}

function status {
    isAlive
    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -ne 200 ]; then
        log "Integration test server is stopped"
    fi
}

function deleteTmpFolder {
    log "deleting temporary folder: $DELETE_TMP_FOLDER"
    rm -rf "$DELETE_TMP_FOLDER"
}

# We use this function to wait for another integration test server to be stopped.
# This can happen on a multi branch pipeline build
#
function waitForStop {

    # init variables
    local secondsToWait=5
    local maxLoop=120
    local timoutSeconds=$((maxLoop*secondsToWait))
    local loopCount=0
    local runningSeconds=0

    until ! isAlive || [ $loopCount -eq $maxLoop ]; do
        sleep $secondsToWait # default suffix for sleep is 's' which means seconds
        loopCount=$((loopCount+1))
        runningSeconds=$((secondsToWait*loopCount))
        log "- waited $runningSeconds/$timoutSeconds seconds for integration test server to shut down"
    done
    if [ $loopCount -eq $maxLoop ] ; then
        log "wait for shutdown of integration test server failed (time out $timoutSeconds seconds reached)"
        log "Please kill the process manually."
        exit 666
    fi
}

# We use this function to wait for another integration test server to be stopped.
# This can happen on a multi branch pipeline build
#
function waitForAlive {

    # init variables
    local server_pid="$1"
    local secondsToWait=5
    local maxLoop=48 # means 48*5 = 240 seconds = 4 minutes max
    local timoutSeconds=$((maxLoop*secondsToWait))
    local loopCount=0
    local runningSeconds=0

    until isAlive || [ $loopCount -eq $maxLoop ]; do
        sleep $secondsToWait # default suffix for sleep is 's' which means seconds
        if [ -n "$server_pid" ] ; then
            if ! kill -0 $server_pid >/dev/null 2>&1 ; then
                echo "ERROR: Java process exited unexpectedly. Please check the logs."
                exit 1
            fi
        fi
        loopCount=$((loopCount+1))
        runningSeconds=$((secondsToWait*loopCount))
        log "- waited $runningSeconds/$timoutSeconds seconds for integration test server to become alive"
    done
    if [ $loopCount -eq $maxLoop ] ; then
        log "wait for integration server failed - time out $timoutSeconds seconds reached. So did not work!"
        exit 666
    fi
}

function stopServer {
    if isAlive ; then
        log "Trying to stop Integration test server"
        SHUTDOWN_HTTP_STATUS=$(curl -k -o /dev/null -I -L -s -w "%{http_code}" https://localhost:$SERVER_PORT/api/anonymous/integrationtest/shutdown)
        log "Shutdown triggered (result http state = $SHUTDOWN_HTTP_STATUS)"
        if [ $SHUTDOWN_HTTP_STATUS -eq 401 ]; then
            log "Integration test server REST not correct implemented - no anonymous access possible! Fix this!"
            exit 666
        fi
        waitForStop
        if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
            log "Integration test server is still alive - shutdown did not work!"
            exit 666
        fi
    fi
    log "Integration test server is stopped"
}


# Starts integration test server, needs server version as first parameter to identify jar to start with...
function startServer {
    if [ -z "$SERVER_VERSION" ] ; then
        log "Server version is missing as second parameter!"
        usage
        exit 1
    fi

    currentDir=$(pwd)
    log "working directory: $currentDir"
    # e.g. curl -sSf https://localhost:8443/api/integrationtest/alive > /dev/null
    isAlive
    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        log "Another integration test server on port $SERVER_PORT is still alive. Going to stop it first."
        stopServer
    fi

    log "Starting a sechub-server (version $SERVER_VERSION) in integration test mode"
    export SERVER_PORT
    export MANAGEMENT_SERVER_PORT
    export SPRING_PROFILES_ACTIVE=integrationtest,mocked_products,h2
    export SECHUB_SERVER_DEBUG=true
    export SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR="$SHARED_VOLUME_BASEDIR"

    pathToJar="../sechub-server/build/libs/sechub-server-$SERVER_VERSION.jar"
    if [ ! -f "$pathToJar" ]; then
        log ">> FAILURE: version not build:$SERVER_VERSION, looked into $pathToJar"
        log ">> Found content inside library folder:"
        ls -al "../sechub-server/build/libs/"
        exit 1
    fi
    pathToLog="$currentDir/integrationtest-server.log"
    if [ -f "$pathToLog" ]; then
        log ">> INFO: removing old logfile: $pathToLog"
        rm "$pathToLog"
    fi
    # Unset proxy so e.g. S3 access will be done without proxy
    export http_proxy=""
    export https_proxy=""
    java -jar "$pathToJar" > "$pathToLog" 2>&1 &
    local sechub_pid=$!
    log ">> INFO: Integration test server has been started"
    log "         logfiles can be found at: $pathToLog"
    log "         ... waiting for server to be up and running ..."
    waitForAlive $sechub_pid
    exit 0
}

function defineSharedVolumeBasePath {
    if [ -z "$1" ] ; then
        SHARED_VOLUME_BASEDIR="$SECHUB_DEFAULT_TEMPFOLDER"
    else
        SHARED_VOLUME_BASEDIR="$1"
    fi
    log "> temporary shared sechub volume dir: \"$SHARED_VOLUME_BASEDIR\""
}

function defineDeleteTmpFolder {
    if [ -z "$1" ] ; then
        log "> no tmpFolder to delete defined - so cannot delete anything!"
        exit 3
    else
        DELETE_TMP_FOLDER="$1"
        log "> tmpFolder to delete: \"$DELETE_TMP_FOLDER\""
    fi
}

function defineServerPort {
    if [ -z "$1" ] ; then
        SERVER_PORT=$SECHUB_DEFAULT_PORT
    else
        SERVER_PORT="$1"
    fi
    log "> server port: $SERVER_PORT"
}

function defineServerVersion {
    if [ -z "$1" ] ; then
        SERVER_VERSION="$SECHUB_DEFAULT_VERSION"
    else
        SERVER_VERSION="$1"
    fi
    log "> server version: \"$SERVER_VERSION\""
}

function defineManagementPort {
    if [ -z "$MANAGEMENT_SERVER_PORT" ] ; then
        MANAGEMENT_SERVER_PORT="$MANAGEMENT_SERVER_PORT_DEFAULT"
    fi
    log "> Spring management port: $MANAGEMENT_SERVER_PORT"
}

function handleArguments {
    SERVER_COMMAND="$1"
    case "$SERVER_COMMAND" in
        start)
            # start version port
            defineServerVersion "$2"
            defineServerPort "$3"
            defineSharedVolumeBasePath "$4"
            defineManagementPort
            ;;
        deleteTmpFolder)
            defineDeleteTmpFolder "$1"
            ;;
        *)
            # other port
            defineServerPort "$2"
            ;;
    esac

    log "Using https://localhost:$SERVER_PORT/ for integration test server"
}

##############################################
if [ -z "$1" ] ; then
    log "command is missing as first parameter!"
    usage
    exit 1
fi

log ">> `basename $0` 1:$1, 2:$2, 3:$3, 4:$4"
log ">> *************************"

if [ "$DEBUG_OUTPUT_ENABLED" = "true" ] ; then
    echo ">> environment variables set:"
    echo "###"
    env | sort
    echo "###"
fi

handleArguments "$1" "$2" "$3" "$4"

case "$SERVER_COMMAND" in
    start) startServer ;;
    stop) stopServer ;;
    waitForStop) waitForStop ;;
    waitForAlive) waitForAlive ;;
    status) status ;;
    deleteTmpFolder) deleteTmpFolder ;;
    *) usage ;;
esac
