#!/bin/bash 

echo "Starting run script:run.sh $1 $2"
# Set debug options if required
if [ x"${JAVA_ENABLE_DEBUG}" != x ] && [ "${JAVA_ENABLE_DEBUG}" != "false" ]; then
    JAVA_DBG_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${JAVA_DEBUG_PORT:-5005}"
fi

if [ -z "$1" ] || [ $1 = "byenv" ]; then
	PROFILE_TO_USE=$SPRING_PROFILE
else
	PROFILE_TO_USE=$1
fi

if [ -z "$2" ] ; then
    JAR_LOCATION="/home/javarun/app.jar"
else
    JAR_LOCATION="$2"
fi

#
# Usage: run.sh demomode -> starts demomode variant (for special java options set please change the JAVA_OPT env)
#        run.sh          -> starts kubernetes setup 
#
#
#

# java.security.edg necessary for optimized random space -> otherwise start is slow because of entropy scanning etc. 
# file encoding per default UTF-8
java $JAVA_DBG_OPTS -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom $SECHUB_OPTS -jar $JAR_LOCATION