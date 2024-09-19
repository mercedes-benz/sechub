# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

ARG PMD_VERSION

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub PMD+PDS Image"
LABEL org.opencontainers.image.description="A container which combines PMD with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

# Environment variables in container
ENV PMD_VERSION="${PMD_VERSION}"

USER root

# Copy mock folder
COPY mocks "$MOCK_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER"/pds-config.json

# Copy PMD scripts and rulesets
COPY scripts "$SCRIPT_FOLDER"

# Set execute permissions for scripts
RUN chmod +x "$SCRIPT_FOLDER"/pmd.sh "$SCRIPT_FOLDER"/pmd_mock.sh

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade && \
    apt-get --assume-yes install w3m wget unzip && \
    apt-get --assume-yes clean

# Install PMD
RUN cd "$DOWNLOAD_FOLDER" && \
    # download pmd
    wget --no-verbose https://github.com/pmd/pmd/releases/download/pmd_releases%2F${PMD_VERSION}/pmd-bin-${PMD_VERSION}.zip && \
    # create pmd folder
    mkdir --parents "$TOOL_FOLDER/pmd/bin" "$TOOL_FOLDER/pmd/lib" && \
    # unpack pmd
    unzip -j pmd-bin-${PMD_VERSION}.zip pmd-bin-${PMD_VERSION}/bin/* -d "$TOOL_FOLDER/pmd/bin" && \
    unzip -j pmd-bin-${PMD_VERSION}.zip pmd-bin-${PMD_VERSION}/lib/* -d "$TOOL_FOLDER/pmd/lib" && \
    # Cleanup download folder
    rm --recursive --force "$DOWNLOAD_FOLDER"/*

# Set workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER "$USER"
