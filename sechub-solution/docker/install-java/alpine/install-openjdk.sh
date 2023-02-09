#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

JAVA_VERSION="$1"
JAVA_RUNTIME="$2"

if [ "$JAVA_RUNTIME" == "jdk" ]
then
    echo "Installing JDK"
    apk add "openjdk$JAVA_VERSION-jdk"
else
    echo "Installing JRE"
    apk add "openjdk$JAVA_VERSION-jre-headless"
fi