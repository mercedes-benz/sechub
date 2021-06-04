#!/bin/bash

# --------------------------------------------------
#  Start / Stop script for integartion test server
# --------------------------------------------------
function usage(){
    echo "usage: integrationtest-pds <cmd=start|stop|waitForStop|waitForAlive|status|checkAlive> {pdsVersion>}|{<pdsPort>}"
    echo "       (PDS version is only necessary for start command"
    echo "       (when no serverPort is set, this port will be used, otherwise 8443 as default)"
}

if [ -z "$1" ] ; then
    echo "command is missing as first parameter!"
    usage
    exit 1
fi

function checkAlive(){
    unset SECHUB_ITS_ALIVE_HTTP_STATUS
    echo "Check alive state"
    SECHUB_ITS_ALIVE_HTTP_STATUS=$(curl -k -o /dev/null -I -L -s -w "%{http_code}" https://localhost:$SERVER_PORT/api/anonymous/integrationtest/alive)
    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        echo "Integration test PDS is alive"
    elif [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 401 ]; then
        echo "Integration test PDS REST not correct implemented - no anonymous access possible! Fix this!"
        exit 666
    else
        echo "-check alive state=$SECHUB_ITS_ALIVE_HTTP_STATUS"
    fi

}

function status(){
    checkAlive
    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        echo "Integration test PDS is running"
    else
        echo "- check alive state=$SECHUB_ITS_ALIVE_HTTP_STATUS"
        echo "Integration test PDS is not running"
    fi
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
        echo "-waited $runningSeconds/$timoutSeconds seconds for other integration test PDS to shutdown"
        loopCount=$((loopCount+1))
    done
    if [ $loopCount -eq $maxLoop ] ; then
        echo "wait for other integration PDS failed- time out $timoutSeconds seconds reached. So did not work!"
        echo "The other process seems to be in 'zombie state' so we force a stop of other PDS."
        stopServer
        echo "-------------------------------------------------"
        echo "Concurrency integration test PDS problem happened"
        echo "-------------------------------------------------"
        echo "We killed another process... Even when it should be okay we must ensure"
        echo "this problem gets attention by developers we terminate with 666..."

        exit 666
    fi
}

# We use this function to wait for another integration test server to be stopped.
# This can happen on a multi branch pipeline build
#
function waitForAlive(){

    # init variables
    secondsToWait=10
    maxLoop=6 # means 6*10 = 60 seconds = 1 minute max
    timoutSeconds=$((maxLoop*secondsToWait))

    loopCount=0
    runningSeconds=0

    until isNotAlive || [ $loopCount -eq $maxLoop ]; do
        sleep $secondsToWait # default suffix for sleep is 's' which means seconds
        runningSeconds=$((secondsToWait*(loopCount+1)))
        echo "-waited $runningSeconds/$timoutSeconds seconds for integration test PDS to become alive"
        loopCount=$((loopCount+1))
    done
    if [ $loopCount -eq $maxLoop ] ; then
        echo "wait for integration PDS failed- time out $timoutSeconds seconds reached. So did not work!"
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
        echo "Try to stop Integration test PDS"
    else
        return 0
    fi
    SHUTDOWN_HTTP_STATUS=$(curl -k -o /dev/null -I -L -s -w "%{http_code}" https://localhost:$SERVER_PORT/api/anonymous/integrationtest/shutdown)
    echo "Shutdown triggered, result http state:$SHUTDOWN_HTTP_STATUS"
    if [ $SHUTDOWN_HTTP_STATUS -eq 401 ]; then
        echo "Integration test PDS REST not correct implemented - no anonymous access possible! Fix this!"
        exit 666
    fi
    checkAlive
    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        echo "Integration test PDS is still alive - shutdown did not work!"
        exit 666

    fi
}


# Starts integration test server, needs server version as first parameter to identify jar to start with...
function startServer(){
    if [ -z "$PDS_VERSION" ] ; then
        echo "Version is missing as second parameter!"
        usage
        exit 1
    fi

    currentDir=$(pwd)
    echo "working directory: $currentDir"
    # e.g. curl -sSf https://localhost:8443/api/integrationtest/alive > /dev/null
    checkAlive

    if [ $SECHUB_ITS_ALIVE_HTTP_STATUS -eq 200 ]; then
        echo "A former integration test PDS is still alive."
        stopServer
    fi

    echo "starting a sechub-pds $PDS_VERSION in integration test mode"
    export SPRING_PROFILES_ACTIVE=pds_integrationtest,pds_h2
    export SECHUB_SERVER_DEBUG=true
    export SECHUB_PDS_STORAGE_SHAREDVOLUME_UPLOAD_DIR=temp

    pathToJar="./../sechub-pds/build/libs/sechub-pds-$PDS_VERSION.jar"
    if [ ! -f $pathToJar ]; then
        echo ">> FAILURE: version not build:$PDS_VERSION, looked into $pathToJar"
        exit 1
    fi
    pathToLog="$currentDir/integrationtest-pds.log"
    if [ -f $pathToLog ]; then
        echo ">> INFO: removing old logfile:$pathToLog"
        rm $pathToLog
    fi
    
    java -jar $pathToJar > $pathToLog &
    echo ">> INFO: integration test PDS has been started"
    echo "         logfiles can be found at: $pathToLog"
    echo "         ... waiting for server up and running ..."
    waitForAlive
    exit 0
}

function defineServerPort(){
    if [ -z "$1" ] ; then
        SERVER_PORT=8543
        echo "> no port defined, using fallback"
    else
        SERVER_PORT="$1"
    fi
}

function defineServerVersion(){
    if [ -z "$1" ] ; then
        PDS_VERSION="0.0.0"
        echo "> no server version defined, using fallback"
    else
        PDS_VERSION="$1"
    fi
}

function handleArguments() {
    SERVER_COMMAND=$1
    case "$SERVER_COMMAND" in
        start)
            # start version port
            defineServerVersion $2
            defineServerPort $3
            ;;
        *)
            # other port
            defineServerPort $2
            ;;
    esac
    if [ -z "$SERVER_PORT" ] ; then
        SERVER_PORT=8444
    fi
    export SERVER_PORT
    echo "Using port $SERVER_PORT for integration test PDS"

}

handleArguments $1 $2 $3

case "$SERVER_COMMAND" in
    start) startServer ;;
    stop) stopServer ;;
    waitForStop) waitForStop ;;
    waitForAlive) waitForAlive ;;
    status) status ;;
    *) usage ;;
esac




