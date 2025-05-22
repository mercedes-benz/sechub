#!/bin/bash
# SPDX-License-Identifier: MIT

LOGFILE="integrationtest-console.log"

# --------------------------------------------------
#  Start / Stop script for integration test PDS
# --------------------------------------------------
cd `dirname $0`

PDS_DEFAULT_PORT=8444
PDS_DEFAULT_VERSION="0.0.0"
PDS_DEFAULT_TEMPFOLDER="temp-shared"
MANAGEMENT_SERVER_PORT_DEFAULT=20251

# --------------------------------------------------------------------------------
# Export special variables to test script environment cleanup works as expected
# - we just start the PDS with those variables
# - only INTEGRATIONTEST_SCRIPT_ENV_ACCEPTED is whitelisted and must be available
#   inside integration tests variable dump, the forbidden one not, because not
#   whitelisted...
# --------------------------------------------------------------------------------
export INTEGRATIONTEST_PDS_STARTED_BY_SCRIPT="true" # is checked in integration tests to check if server started by script
export INTEGRATIONTEST_SCRIPT_ENV_ACCEPTED="accepted"
export INTEGRATIONTEST_SCRIPT_ENV_FORBIDDEN="forbidden"

function log {
    echo "$1"
    echo "`date +%Y-%m-%d\ %H:%M:%S` $1" >> "$LOGFILE"
}

function usage {
    log "usage: `basename $0` start [<pdsVersion>] [<pdsPort>] [<sharedTempSharedVolumeFolder>]"
    log "       `basename $0` stop|status|waitForStop|waitForAlive [<pdsPort>]"
    log "       Control local PDS for e.g. integrationtests"
    log "       Defaults:"
    log "       - pds version \"$PDS_DEFAULT_VERSION\""
    log "       - pds port \"$PDS_DEFAULT_PORT\""
    log "       - temp shared volume folder: \"$PDS_DEFAULT_TEMPFOLDER\""
    log "       - Spring management port: $MANAGEMENT_SERVER_PORT_DEFAULT (can be overridden with env var MANAGEMENT_SERVER_PORT)"
}

function isAlive {
    unset PDS_ITS_ALIVE_HTTP_STATUS
    log "Checking alive state"
    PDS_ITS_ALIVE_HTTP_STATUS=$(curl -k -o /dev/null -I -L -s -w "%{http_code}" https://localhost:$PDS_PORT/api/anonymous/integrationtest/alive)
    if [ $PDS_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        log "-> Integration test PDS is running"
        return 0
    elif [ $PDS_ITS_ALIVE_HTTP_STATUS -eq 401 ]; then
        log "-> Integration test PDS REST not correct implemented - no anonymous access possible! Fix this!"
        exit 666
    else
        log "-> Integration test PDS is not responding (state = $PDS_ITS_ALIVE_HTTP_STATUS)"
    fi
    # PDS is not running
    return 1
}

function status {
    isAlive
    if [ $PDS_ITS_ALIVE_HTTP_STATUS -ne 200 ]; then
        log "Integration test PDS is stopped"
    fi
}

# We use this function to wait for another integration test PDS to be stopped.
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
        log "- waited $runningSeconds/$timoutSeconds seconds for integration test PDS to shut down"
    done
    if [ $loopCount -eq $maxLoop ] ; then
        log "wait for shutdown of integration test PDS failed (time out $timoutSeconds seconds reached)"
        log "Please kill the process manually."
        exit 666
    fi
}

# We use this function to wait for another integration test PDS to be stopped.
# This can happen on a multi branch pipeline build
#
function waitForAlive {

    # init variables
    local server_pid="$1"
    local secondsToWait=5
    local maxLoop=12 # means 12*5 = 60 seconds = 1 minute max
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
        log "- waited $runningSeconds/$timoutSeconds seconds for integration test PDS to become alive"
    done
    if [ $loopCount -eq $maxLoop ] ; then
        log "wait for integration PDS failed - time out $timoutSeconds seconds reached. So did not work!"
        exit 666
    fi
}

function stopServer {
    if isAlive ; then
        log "Trying to stop Integration test PDS"
        SHUTDOWN_HTTP_STATUS=$(curl -k -o /dev/null -I -L -s -w "%{http_code}" https://localhost:$PDS_PORT/api/anonymous/integrationtest/shutdown)
        log "Shutdown triggered (result http state = $SHUTDOWN_HTTP_STATUS)"
        if [ $SHUTDOWN_HTTP_STATUS -eq 401 ]; then
            log "Integration test PDS REST not correct implemented - no anonymous access possible! Fix this!"
            exit 666
        fi
        waitForStop
        if [ $PDS_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
            log "Integration test PDS is still alive - shutdown did not work!"
            exit 666
        fi
    fi
    log "Integration test PDS is stopped"
}


# Starts integration test PDS, needs PDS version as first parameter to identify jar to start with...
function startServer {
    if [ -z "$PDS_VERSION" ] ; then
        log "PDS version is missing as second parameter!"
        usage
        exit 1
    fi

    currentDir=$(pwd)
    log "working directory: $currentDir"
    # e.g. curl -sSf https://localhost:8443/api/integrationtest/alive > /dev/null
    isAlive
    if [ $PDS_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        log "Another integration test PDS on port $PDS_PORT is still alive. Going to stop it first."
        stopServer
    fi

    log "Starting a sechub-pds (version $PDS_VERSION) in integration test mode"
    export SERVER_PORT=$PDS_PORT
    export MANAGEMENT_SERVER_PORT
    export SPRING_PROFILES_ACTIVE=pds_integrationtest,pds_h2
    export SECHUB_SERVER_DEBUG=true
    export PDS_STORAGE_SHAREDVOLUME_UPLOAD_DIR="$SHARED_VOLUME_BASEDIR"

    pathToJar="../sechub-pds/build/libs/sechub-pds-$PDS_VERSION.jar"
    if [ ! -f "$pathToJar" ]; then
        log ">> FAILURE: version not build:$PDS_VERSION, looked into $pathToJar"
        log ">> Found content inside library folder:"
        ls -al "../sechub-pds/build/libs/"
        exit 1
    fi
    pathToLog="$currentDir/integrationtest-pds.log"
    if [ -f "$pathToLog" ]; then
        log ">> INFO: removing old logfile: $pathToLog"
        rm "$pathToLog"
    fi
    # Unset proxy so e.g. S3 access will be done without proxy
    export http_proxy=""
    export https_proxy=""
    java -jar "$pathToJar" > "$pathToLog" 2>&1 &
    local pds_pid=$!
    log ">> INFO: Integration test PDS has been started"
    log "         logfiles can be found at: $pathToLog"
    log "         ... waiting for PDS to be up and running ..."
    waitForAlive $pds_pid
    exit 0
}

function defineSharedVolumeBasePath {
    if [ -z "$1" ] ; then
        SHARED_VOLUME_BASEDIR="$PDS_DEFAULT_TEMPFOLDER"
    else
        SHARED_VOLUME_BASEDIR="$1"
    fi
    log "> temporary shared sechub volume dir: \"$SHARED_VOLUME_BASEDIR\""
}

function definePDSPort {
    if [ -z "$1" ] ; then
        PDS_PORT=$PDS_DEFAULT_PORT
    else
        PDS_PORT="$1"
    fi
    log "> PDS port: $PDS_PORT"
}

function definePDSVersion {
    if [ -z "$1" ] ; then
         PDS_VERSION="$PDS_DEFAULT_VERSION"
    else
         PDS_VERSION="$1"
    fi
    log "> PDS version: \"$PDS_VERSION\""
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
            definePDSVersion "$2"
            definePDSPort "$3"
            defineSharedVolumeBasePath "$4"
            defineManagementPort
            ;;
        *)
            # other port
            definePDSPort "$2"
            ;;
    esac

    log "Using https://localhost:$PDS_PORT/ for integration test PDS"
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
    *) usage ;;
esac
