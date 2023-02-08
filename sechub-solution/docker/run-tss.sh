#!/bin/bash

error_handling() {
    echo "last exit code: ${?}"
    echo "SecHub run.sh failed on line $(caller)"
    # Only for debugging:
    # define KUBE_ENABLE_DEBUG_WAIT as "true" inside your deployment and container will wait 2 hours before going down
    if [ "${KUBE_ENABLE_DEBUG_WAIT}" == "true" ]; then
        echo "KUBE_ENABLE_DEBUG_WAIT is enabled: sleeping 2h before exiting."
        sleep 2h
    fi
}

trap error_handling ERR

echo "Starting run script: run.sh $@"

if [ -z "$SECHUB_CONFIG_TRIGGER_NEXTJOB_DELAY" ] ; then
    SECHUB_CONFIG_TRIGGER_NEXTJOB_DELAY="10000"
    echo "---> SECHUB_CONFIG_TRIGGER_NEXTJOB_DELAY not defined! Falling back to $SECHUB_CONFIG_TRIGGER_NEXTJOB_DELAY"
fi
echo "SECHUB_CONFIG_TRIGGER_NEXTJOB_DELAY = $SECHUB_CONFIG_TRIGGER_NEXTJOB_DELAY"

amountOn303=$(( $SECHUB_CONFIG_TRIGGER_NEXTJOB_DELAY / 303))
echo "SECHUB_CONFIG_TRIGGER_NEXTJOB_DELAY / 303   = $amountOn303"

# Randomize initial delay used for trigger next job
#
# So when having SECHUB_CONFIG_TRIGGER_NEXTJOB_DELAY with 10.000 (is default) we got amountOf303 with33
# which means min 303 millis, max 33*303=9999 milis
export SECHUB_CONFIG_TRIGGER_NEXTJOB_INITIALDELAY=$(( $(shuf -i 1-$amountOn303 -n 1) * 303 ))
echo "SECHUB_CONFIG_TRIGGER_NEXTJOB_INITIALDELAY = $SECHUB_CONFIG_TRIGGER_NEXTJOB_INITIALDELAY";

if [ -n "${SECHUB_SERVER_SSL_KEYSTORE_ALIAS}" -a "${SECHUB_SERVER_SSL_KEYSTORE_ALIAS}" != "undefined" ]; then
    echo "- linking certificate file by using secret SSL content and alias '${SECHUB_SERVER_SSL_KEYSTORE_ALIAS}'"
    # create a symlink to keystore file
    ln -s /sechub/secrets/secret-ssl/sechub_server_ssl_keystore_file ${SECHUB_SERVER_SSL_KEYSTORE_LOCATION}
fi

# Set Java debug options if required
if [ "${JAVA_ENABLE_DEBUG}" == "true" ]; then
    JAVA_DBG_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${JAVA_DEBUG_PORT:-5005}"
fi

# UNGENUTZT
if [ -z "$1" ] || [ $1 = "byenv" ]; then
	PROFILE_TO_USE=$SPRING_PROFILE
else
	PROFILE_TO_USE=$1
fi

# $2 ist immer leer - oder?
if [ -z "$2" ] ; then
    JAR_LOCATION="/home/javarun/app.jar"
else
    JAR_LOCATION="$2"
fi

# java.security.edg necessary for optimized random space -> otherwise start is slow becauase of entropy scanning etc.
# file encoding per default UTF-8
# set the initial RAM to 50% and the maximum RAM to 80%.
echo "- starting sechub server"
java $JAVA_DBG_OPTS -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/urandom $SECHUB_OPTS -XX:InitialRAMPercentage=50 -XX:MaxRAMPercentage=80 -XshowSettings:vm -jar $JAR_LOCATION
