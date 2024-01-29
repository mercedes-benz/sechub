#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

JAVA_VERSION="$1"
JAVA_RUNTIME="$2"

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install --assume-yes "openjdk-$JAVA_VERSION-$JAVA_RUNTIME-headless"
apt-get clean