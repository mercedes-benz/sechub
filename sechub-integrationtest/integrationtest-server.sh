#!/bin/bash
# SPDX-License-Identifier: MIT

# define fd 3 to log into console and log file
exec 3>&1 1>>integrationtest-console.log 2>&1

# --------------------------------------------------
#  Start / Stop script for integartion test server
# --------------------------------------------------
cd `dirname $0`

function log() {
    echo $1 | tee /dev/fd/3
}

function usage(){
    log "usage: integrationtest-server <cmd=start|stop|waitForStop|waitForAlive|status|checkAlive> {<serverVersion>}|{<serverPort>}{sharedTempSharedVolumeFolder}"
    log "       (server version is only necessary for start command"
    log "       (when no serverPort is set, this port will be used, otherwise 8443 as default)"
}

if [ -z "$1" ] ; then
    log "command is missing as first parameter!"
    usage
    exit 1
fi

function checkAlive(){
    unset SECHUB_ITS_ALIVE_HTTP_STATUS
    log "Check alive state"
    SECHUB_ITS_ALIVE_HTTP_STATUS=$(curl -k -o /dev/null -I -L -s -w "%{http_code}" https://localhost:$SERVER_PORT/api/anonymous/integrationtest/alive)
    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        log "Integration test server is alive"
    elif [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 401 ]; then
        log "Integration test server REST not correct implemented - no anonymous access possible! Fix this!"
        exit 666
    else
        log "-check alive state=$SECHUB_ITS_ALIVE_HTTP_STATUS"
    fi

}

function status(){
    checkAlive
    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        log "Integration test server is running"
    else
        log "- check alive state=$SECHUB_ITS_ALIVE_HTTP_STATUS"
        log "Integration test server is not running"
    fi
}

function deleteTmpFolder(){
    log "start delete folder: $DELETE_TMP_FOLDER"
    rm "$DELETE_TMP_FOLDER" -rf 
    
}

# We use this function to wait for another integration test server to be stopped.
# This can happen on a multi branch pipeline build
#
function waitForStop(){

    # init variables
    secondsToWait=30
    maxLoop=20
    timoutSeconds=$((maxLoop*secondsToWait))

    loopCount=0
    runningSeconds=0

    until isAlive || [ $loopCount -eq $maxLoop ]; do
        sleep $secondsToWait # default suffix for sleep is 's' which means seconds
        runningSeconds=$((secondsToWait*(loopCount+1)))
        log "-waited $runningSeconds/$timoutSeconds seconds for other integration test server to shutdown"
        loopCount=$((loopCount+1))
    done
    if [ $loopCount -eq $maxLoop ] ; then
        log "wait for other integration server failed- time out $timoutSeconds seconds reached. So did not work!"
        log "The other process seems to be in 'zombie state' so we force a stop of other server."
        stopServer
        log "----------------------------------------------------"
        log "Concurrency integration test server problem happened"
        log "----------------------------------------------------"
        log "We killed another process... Even when it should be okay we must ensure"
        log "this problem gets attention by developers we terminate with 666..."

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
        log "-waited $runningSeconds/$timoutSeconds seconds for integration test server to become alive"
        loopCount=$((loopCount+1))
    done
    if [ $loopCount -eq $maxLoop ] ; then
        log "wait for integration server failed- time out $timoutSeconds seconds reached. So did not work!"
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
        log "Try to stop Integration test server"
    else
        return 0
    fi
    SHUTDOWN_HTTP_STATUS=$(curl -k -o /dev/null -I -L -s -w "%{http_code}" https://localhost:$SERVER_PORT/api/anonymous/integrationtest/shutdown)
    log "Shutdown triggered, result http state:$SHUTDOWN_HTTP_STATUS"
    if [ $SHUTDOWN_HTTP_STATUS -eq 401 ]; then
        log "Integration test server REST not correct implemented - no anonymous access possible! Fix this!"
        exit 666
    fi
    checkAlive
    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        log "Integration test server is still alive - shutdown did not work!"
        exit 666

    fi
}


# Starts integration test server, needs server version as first parameter to identify jar to start with...
function startServer(){
    if [ -z "$SERVER_VERSION" ] ; then
        log "Version is missing as second parameter!"
        usage
        exit 1
    fi

    currentDir=$(pwd)
    log "working directory: $currentDir"
    # e.g. curl -sSf https://localhost:8443/api/integrationtest/alive > /dev/null
    checkAlive

    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        log "A former integration test server is still alive."
        stopServer
    fi

    log "starting a sechub-server $SERVER_VERSION in integration test mode"
    export SPRING_PROFILES_ACTIVE=integrationtest,mocked_products,h2
    export SECHUB_SERVER_DEBUG=true
    export SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR="$SHARED_VOLUME_BASEDIR"

    pathToJar="./../sechub-server/build/libs/sechub-server-$SERVER_VERSION.jar"
    if [ ! -f $pathToJar ]; then
        log ">> FAILURE: version not build:$SERVER_VERSION, looked into $pathToJar"
        log ">> Found content inside library folder:"
        ls -all "./../sechub-server/build/libs/"
        exit 1
    fi
    pathToLog="$currentDir/integrationtest-server.log"
    if [ -f $pathToLog ]; then
        log ">> INFO: removing old logfile:$pathToLog"
        rm $pathToLog
    fi
    java -jar $pathToJar > $pathToLog &
    log ">> INFO: integration test server has been started"
    log "         logfiles can be found at: $pathToLog"
    log "         ... waiting for server up and running ..."
    waitForAlive
    exit 0
}

function defineSharedVolumeBasePath(){
    if [ -z "$1" ] ; then
        SHARED_VOLUME_BASEDIR="temp"
        log "> no shared sechub volume defined, using fallback:temp"
    else
        SHARED_VOLUME_BASEDIR="$1"
        log "> shared sechub volume defined with: $SHARED_VOLUME_BASEDIR"
    fi
}

function defineDeleteTmpFolder(){
    if [ -z "$1" ] ; then
        log "> no tmpFolder to delete deinfed - so cannot delete anything!"
        exit 3
    else
        DELETE_TMP_FOLDER="$1"
        log "> tmpFolder to delete defined with: $DELETE_TMP_FOLDER"
    fi
}

function defineServerPort(){
    if [ -z "$1" ] ; then
        SERVER_PORT=8443
        log "> no port defined, using fallback"
    else
        SERVER_PORT="$1"
    fi
}

function defineServerVersion(){
    if [ -z "$1" ] ; then
        SERVER_VERSION="0.0.0"
        log "> no server version defined, using fallback"
    else
        SERVER_VERSION="$1"
    fi
}

function handleArguments() {
    SERVER_COMMAND=$1
    case "$SERVER_COMMAND" in
        start)
            # start version port
            defineServerVersion $2
            defineServerPort $3
            defineSharedVolumeBasePath $4
            ;;
        deleteTmpFolder)
            defineDeleteTmpFolder $1
            ;;
        *)
            # other port
            defineServerPort $2
            ;;
    esac
    if [ -z "$SERVER_PORT" ] ; then
        SERVER_PORT=8443
    fi

    log "Using port $SERVER_PORT for integration test server"

}
log ">> integrationtest-server.sh 1:$1, 2:$2, 3:$3, 4:$4"
log ">> *************************"

handleArguments $1 $2 $3 $4

case "$SERVER_COMMAND" in
    start) startServer ;;
    stop) stopServer ;;
    waitForStop) waitForStop ;;
    waitForAlive) waitForAlive ;;
    status) status ;;
    deleteTmpFolder) deleteTmpFolder ;;
    *) usage ;;
esac
