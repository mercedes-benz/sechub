#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

JAVA_VERSION="$1"
JAVA_RUNTIME="$2"

dnf update --assumeyes

if [ "$JAVA_RUNTIME" == "jdk" ]
then
    dnf install --assumeyes "java-$JAVA_VERSION-openjdk-devel"
else
    dnf install --assumeyes "java-$JAVA_VERSION-openjdk-headless"
fi

dnf clean all