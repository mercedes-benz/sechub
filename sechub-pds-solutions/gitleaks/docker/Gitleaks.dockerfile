# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE

FROM ${BASE_IMAGE}

ENV GITLEAKS_VERSION="8.8.11"

USER root

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install --assume-yes --quiet wget git openssh-client && \
    apt-get clean

RUN cd "$DOWNLOAD_FOLDER" && \
    wget "https://github.com/zricethezav/gitleaks/releases/download/v${GITLEAKS_VERSION}/gitleaks_${GITLEAKS_VERSION}_checksums.txt" && \
    wget "https://github.com/zricethezav/gitleaks/releases/download/v${GITLEAKS_VERSION}/gitleaks_${GITLEAKS_VERSION}_linux_x64.tar.gz" && \
    sha256sum --check --ignore-missing "gitleaks_${GITLEAKS_VERSION}_checksums.txt" && \
    tar --extract --gunzip --file="gitleaks_${GITLEAKS_VERSION}_linux_x64.tar.gz" --directory="$TOOL_FOLDER" && \
    rm --recursive --force "$DOWNLOAD_FOLDER/*"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER"/pds-config.json

# Copy scripts
COPY scripts/ "$SCRIPT_FOLDER"

# Copy mocks
COPY mocks "$MOCK_FOLDER"

USER "$USER"