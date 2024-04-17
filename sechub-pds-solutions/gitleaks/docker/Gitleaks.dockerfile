# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE

FROM ${BASE_IMAGE}

ARG GITLEAKS_VERSION=8.16.4
ENV GITLEAKS_VERSION="${GITLEAKS_VERSION}"

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
    rm --recursive --force "$DOWNLOAD_FOLDER"/*

# Copy custom rule file custom-gitleaks.toml
COPY custom-gitleaks.toml "$TOOL_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER"/pds-config.json

# Copy scripts
COPY --chmod=0755 --chown="$USER:$USER" scripts/ "$SCRIPT_FOLDER"

# Copy run_additional script
COPY --chmod=0755 --chown="$USER:$USER" run_additional.sh /run_additional.sh

# Copy mocks
COPY mocks "$MOCK_FOLDER"

USER "$USER"
