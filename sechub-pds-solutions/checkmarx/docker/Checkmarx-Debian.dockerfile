# SPDX-License-Identifier: MIT

#-------------------
# Global Variables
#-------------------

# The image argument needs to be placed on top
ARG BASE_IMAGE

# Build args
ARG BUILD_TYPE=download
ARG CHECKMARX_WRAPPER_VERSION

# The base image of the builder
ARG BUILDER_BASE_IMAGE="debian:12-slim"

# Artifact folder
ARG ARTIFACT_FOLDER="/artifacts"

#-------------------
# Builder Download
#-------------------

FROM ${BUILDER_BASE_IMAGE} AS builder-download

ARG ARTIFACT_FOLDER
ARG CHECKMARX_WRAPPER_VERSION

RUN mkdir --parent "$ARTIFACT_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install --assume-yes wget && \
    apt-get clean

# Download the Checkmarx Wrapper
RUN cd "$ARTIFACT_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$CHECKMARX_WRAPPER_VERSION-checkmarx-wrapper/sechub-wrapper-checkmarx-$CHECKMARX_WRAPPER_VERSION.jar.sha256sum" && \
    # download wrapper jar
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$CHECKMARX_WRAPPER_VERSION-checkmarx-wrapper/sechub-wrapper-checkmarx-$CHECKMARX_WRAPPER_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check "sechub-wrapper-checkmarx-$CHECKMARX_WRAPPER_VERSION.jar.sha256sum"

#-------------------
# Builder Copy Jar
#-------------------

FROM ${BUILDER_BASE_IMAGE} AS builder-copy

ARG ARTIFACT_FOLDER

RUN mkdir --parent "$ARTIFACT_FOLDER"

# Copy
COPY copy/sechub-wrapper-checkmarx-* "$ARTIFACT_FOLDER"

#-------------------
# Builder
#-------------------

FROM builder-${BUILD_TYPE} as builder
RUN echo "build stage"

#-------------------
# PDS + Checkmarx Image
#-------------------

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Checkmarx+PDS Image"
LABEL org.opencontainers.image.description="A container which combines a Checkmarx Wrapper script with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

ARG ARTIFACT_FOLDER

USER root

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get upgrade --assume-yes && \
    apt-get clean

COPY --from=builder "$ARTIFACT_FOLDER" "$TOOL_FOLDER"
RUN ln --symbolic $TOOL_FOLDER/sechub-wrapper-checkmarx-*.jar $TOOL_FOLDER/sechub-wrapper-checkmarx.jar

# Copy mock folders
COPY mocks/ "$MOCK_FOLDER"

# Copy scripts
COPY scripts $SCRIPT_FOLDER
RUN chmod --recursive +x "$SCRIPT_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER/pds-config.json"

# Switch from root to non-root user
USER "$USER"