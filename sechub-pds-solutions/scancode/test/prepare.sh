#!/bin/bash
# SPDX-License-Identifier: MIT

DOWNLOAD_FOLDER="downloads"
IMAGE_NAME="Armbian_22.08.1_Jetson-nano_jammy_current_5.18.19.img"
PDS_TOOLS_VERSION="0.1.0"
PDS_TOOLS_JAR="sechub-pds-tools-cli-${PDS_TOOLS_VERSION}.jar"

function download_image() {
    cd "$DOWNLOAD_FOLDER"
    # download image
    wget --show-progress https://fi.mirror.armbian.de/dl/jetson-nano/archive/"${IMAGE_NAME}.xz"
    # download checksum
    wget --show-progress https://fi.mirror.armbian.de/dl/jetson-nano/archive/"${IMAGE_NAME}.xz.sha"
    sha256sum -c "${IMAGE_NAME}.xz.sha"
}

function extract_image() {
    unxz "${IMAGE_NAME}.xz"
}

function download_pds_tools() {
    wget "https://github.com/mercedes-benz/sechub/releases/download/v${PDS_TOOLS_VERSION}-pds-tools/sechub-pds-tools-cli-${PDS_TOOLS_VERSION}.jar"
    wget "https://github.com/mercedes-benz/sechub/releases/download/v${PDS_TOOLS_VERSION}-pds-tools/sechub-pds-tools-cli-${PDS_TOOLS_VERSION}.jar.sha256sum"
    sha256sum -c "${PDS_TOOLS_JAR}.sha256sum"
}

mkdir -p "$DOWNLOAD_FOLDER"
cd "$DOWNLOAD_FOLDER"

if [[ -f "$IMAGE_NAME" ]]
then
    echo "File already exists"
else 
    echo "Downloading file"
    download_image
    echo "Extracting file"
    extract_image
fi

if [[ -f "$PDS_TOOLS_JAR" ]]
then
    echo "PDS Tools already downloaded"
else
    echo "Dowloading PDS Tools"
    download_pds_tools
fi

cp ../sechub-config.json .

java -jar "sechub-pds-tools-cli-$PDS_TOOLS_VERSION.jar" --generate "sechub-config.json" "licenseScan" .