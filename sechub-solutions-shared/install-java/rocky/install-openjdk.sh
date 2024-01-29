#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

JAVA_VERSION="$1"
JAVA_RUNTIME="$2"

microdnf update --assumeyes

if [ "$JAVA_RUNTIME" == "jdk" ]
then
    microdnf install --assumeyes "java-$JAVA_VERSION-openjdk-devel"
else
    microdnf install --assumeyes "java-$JAVA_VERSION-openjdk-headless"
fi

microdnf clean all