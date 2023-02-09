#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

JAVA_VERSION="$1"
JAVA_RUNTIME="$2"

apk add wget

wget -O /etc/apk/keys/adoptium.rsa.pub https://packages.adoptium.net/artifactory/api/security/keypair/public/repositories/apk
echo 'https://packages.adoptium.net/artifactory/apk/alpine/main' >> /etc/apk/repositories

# Temurin does not have JRE build in the Linux packages: https://github.com/adoptium/installer/issues/430
apk add temurin-"${JAVA_VERSION}-${JAVA_RUNTIME}"