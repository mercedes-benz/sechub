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

function log() {
    echo "$1"
    echo "`date +%Y-%m-%d\ %H:%M:%S` $1" >> "$LOGFILE"
}

function usage(){
    log "usage: `basename $0` start [<serverVersion>] [<serverPort>] [<sharedTempSharedVolumeFolder>]"
    log "       `basename $0` stop|status|waitForStop|waitForAlive [<serverPort>]"
    log "       Control local SecHub server for e.g. integrationtests"
    log "       Defaults:"
    log "       - serverVersion \"$SECHUB_DEFAULT_VERSION\""
    log "       - serverPort \"$SECHUB_DEFAULT_PORT\""
    log "       - sharedTempSharedVolumeFolder \"$SECHUB_DEFAULT_TEMPFOLDER\""
}

if [ -z "$1" ] ; then
    log "command is missing as first parameter!"
    usage
    exit 1
fi

function checkAlive(){
    unset SECHUB_ITS_ALIVE_HTTP_STATUS
    log "Checking alive state"
    SECHUB_ITS_ALIVE_HTTP_STATUS=$(curl -k -o /dev/null -I -L -s -w "%{http_code}" https://localhost:$SERVER_PORT/api/anonymous/integrationtest/alive)
    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        log "-> Integration test server is running"
    elif [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 401 ]; then
        log "-> Integration test server REST not correct implemented - no anonymous access possible! Fix this!"
        exit 666
    else
        log "-> Integration test server is not responding (state = $SECHUB_ITS_ALIVE_HTTP_STATUS)"
    fi
}

function status(){
    checkAlive
    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -ne 200 ]; then
        log "Integration test server is stopped"
    fi
}

function deleteTmpFolder(){
    log "deleting temporary folder: $DELETE_TMP_FOLDER"
    rm -rf "$DELETE_TMP_FOLDER"
}

# We use this function to wait for another integration test server to be stopped.
# This can happen on a multi branch pipeline build
#
function waitForStop(){

    # init variables
    local secondsToWait=30
    local maxLoop=20
    local timoutSeconds=$((maxLoop*secondsToWait))

    local loopCount=0
    local runningSeconds=0

    until isAlive || [ $loopCount -eq $maxLoop ]; do
        sleep $secondsToWait # default suffix for sleep is 's' which means seconds
        runningSeconds=$((secondsToWait*(loopCount+1)))
        log "- waited $runningSeconds/$timoutSeconds seconds for integration test server to shut down"
        loopCount=$((loopCount+1))
    done
    if [ $loopCount -eq $maxLoop ] ; then
        log "wait for shutdown of integration test server failed (time out $timoutSeconds seconds reached)"
        log "The process seems to be in 'zombie state'. Please kill the process manually."
        exit 666
    fi
}

# We use this function to wait for another integration test server to be stopped.
# This can happen on a multi branch pipeline build
#
function waitForAlive(){

    # init variables
    secondsToWait=10
    maxLoop=24 # means 24*10 = 240 seconds = 4 minutes max
    timoutSeconds=$((maxLoop*secondsToWait))

    loopCount=0
    runningSeconds=0

    until isNotAlive || [ $loopCount -eq $maxLoop ]; do
        sleep $secondsToWait # default suffix for sleep is 's' which means seconds
        runningSeconds=$((secondsToWait*(loopCount+1)))
        log "- waited $runningSeconds/$timoutSeconds seconds for integration test server to become alive"
        loopCount=$((loopCount+1))
    done
    if [ $loopCount -eq $maxLoop ] ; then
        log "wait for integration server failed - time out $timoutSeconds seconds reached. So did not work!"
        exit 666
    fi
}

function isAlive(){
    checkAlive
    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        return 1
    fi
    return 0
}

function isNotAlive(){
    checkAlive
    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        return 0
    fi
    return 1
}

function stopServer(){
    checkAlive
    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
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
function startServer(){
    if [ -z "$SERVER_VERSION" ] ; then
        log "Server version is missing as second parameter!"
        usage
        exit 1
    fi

    currentDir=$(pwd)
    log "working directory: $currentDir"
    # e.g. curl -sSf https://localhost:8443/api/integrationtest/alive > /dev/null
    checkAlive
    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        log "Another integration test server on port $SERVER_PORT is still alive. Going to stop it first."
        stopServer
    fi

    log "Starting a sechub-server (version $SERVER_VERSION) in integration test mode"
    export SERVER_PORT
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
    log ">> INFO: Integration test server has been started"
    log "         logfiles can be found at: $pathToLog"
    log "         ... waiting for server to be up and running ..."
    waitForAlive
    exit 0
}

function defineSharedVolumeBasePath(){
    if [ -z "$1" ] ; then
        SHARED_VOLUME_BASEDIR="$SECHUB_DEFAULT_TEMPFOLDER"
        log "> no shared sechub volume defined, using fallback: \"$SHARED_VOLUME_BASEDIR\""
    else
        SHARED_VOLUME_BASEDIR="$1"
        log "> shared sechub volume defined with: $SHARED_VOLUME_BASEDIR"
    fi
}

function defineDeleteTmpFolder(){
    if [ -z "$1" ] ; then
        log "> no tmpFolder to delete defined - so cannot delete anything!"
        exit 3
    else
        DELETE_TMP_FOLDER="$1"
        log "> tmpFolder to delete defined with: $DELETE_TMP_FOLDER"
    fi
}

function defineServerPort(){
    if [ -z "$1" ] ; then
        SERVER_PORT=$SECHUB_DEFAULT_PORT
        log "> no port defined, using fallback: port $SERVER_PORT"
    else
        SERVER_PORT="$1"
    fi
}

function defineServerVersion(){
    if [ -z "$1" ] ; then
        SERVER_VERSION="$SECHUB_DEFAULT_VERSION"
        log "> no server version defined, using fallback: \"$SERVER_VERSION\""
    else
        SERVER_VERSION="$1"
    fi
}

function handleArguments() {
    SERVER_COMMAND="$1"
    case "$SERVER_COMMAND" in
        start)
            # start version port
            defineServerVersion "$2"
            defineServerPort "$3"
            defineSharedVolumeBasePath "$4"
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
