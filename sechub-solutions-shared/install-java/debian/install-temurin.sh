#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

JAVA_VERSION="$1"
JAVA_RUNTIME="$2"

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install --assume-yes wget apt-transport-https

mkdir -p /etc/apt/keyrings
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /etc/apt/keyrings/adoptium.asc
echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" \
| tee /etc/apt/sources.list.d/adoptium.list

apt-get update

# Temurin does not have JRE build in the Linux packages: https://github.com/adoptium/installer/issues/430
apt-get install --assume-yes --quiet temurin-"${JAVA_VERSION}-${JAVA_RUNTIME}"
apt-get remove --assume-yes --quiet wget
apt-get clean