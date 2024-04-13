#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

function start_pds() {
    echo "Starting script."
    "../05-start-single-sechub-network-docker-compose.sh"
}

cd $(dirname "$0")

if [[ -z "$BUILD_TYPE" ]]
then
	echo "BUILD_TYPE not defined"
	exit 1
fi

if [[ -z "$PDS_VERSION" ]]
then
	echo "PDS_VERSION not defined"
	exit 1
fi

echo "BUILD_TYPE: $BUILD_TYPE"
echo "PDS_VERSION: $PDS_VERSION"

case "$BUILD_TYPE" in
  "copy")
  	echo "Copying files"
	wget "https://github.com/mercedes-benz/sechub/releases/download/v${PDS_VERSION}-pds/sechub-pds-${PDS_VERSION}.jar" 
  	mv "sechub-pds-${PDS_VERSION}.jar" --target-directory "../docker/copy/"
  	start_pds
    ;;
  "download" | "build")
	start_pds
    ;;
  *)
  	echo "Unknown BUILD_TYPE"
    ;;
esac