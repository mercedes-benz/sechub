# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE

# Build Args
# Build type can be "copy" or "download"
ARG BUILD_TYPE
ARG PREPARE_WRAPPER_VERSION="1.0.0"

# The base image of the builder
ARG BUILDER_BASE_IMAGE="debian:12-slim"
ARG ARTIFACT_FOLDER="/artifacts"


#-------------------
# Builder Download
#-------------------
# (downloads a released prepare-Wrapper jar)

FROM ${BUILDER_BASE_IMAGE} AS builder-download

ARG ARTIFACT_FOLDER
ARG PREPARE_WRAPPER_VERSION

RUN mkdir --parent "$ARTIFACT_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install --assume-yes wget && \
    apt-get -y install skopeo && \
    apt-get -y install git && \
    apt-get clean

# Download the prepare Wrapper
RUN cd "$ARTIFACT_FOLDER" && \
    # download wrapper jar
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PREPARE_WRAPPER_VERSION-prepare-wrapper/sechub-pds-wrapper-prepare-$PREPARE_WRAPPER_VERSION.jar" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PREPARE_WRAPPER_VERSION-prepare-wrapper/sechub-pds-wrapper-prepare-$PREPARE_WRAPPER_VERSION.jar.sha256sum" && \
    # verify the checksum
    sha256sum --check "sechub-pds-wrapper-prepare-$PREPARE_WRAPPER_VERSION.jar.sha256sum"


#-------------------
# Builder Copy Jar
#-------------------
# (copies the prepare-Wrapper jar from local subdirectory "copy")

FROM ${BUILDER_BASE_IMAGE} AS builder-copy

ARG ARTIFACT_FOLDER
ARG PREPARE_WRAPPER_VERSION

RUN mkdir --parent "$ARTIFACT_FOLDER"

# Copy
COPY copy/sechub-pds-wrapper-prepare-$PREPARE_WRAPPER_VERSION.jar "$ARTIFACT_FOLDER"


#-------------------
# Builder
#-------------------

FROM builder-${BUILD_TYPE} as builder
RUN echo "build stage"


#-------------------
# PDS + Prepare Image
#-------------------

FROM ${BASE_IMAGE}

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Prepare+PDS Image"
LABEL org.opencontainers.image.description="A container which combines Prepare Wrapper with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

ARG ARTIFACT_FOLDER
ARG PREPARE_WRAPPER_VERSION

USER root

# Copy mock folder
COPY mocks "$MOCK_FOLDER"

# Copy scripts
COPY scripts "$SCRIPT_FOLDER"
RUN chmod --recursive +x "$SCRIPT_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER/pds-config.json"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade && \
    apt-get -y install skopeo git && \
    apt-get --assume-yes clean

# Copy Prepare-Wrapper jar from builder
COPY --from=builder "$ARTIFACT_FOLDER" "$TOOL_FOLDER"
RUN ln -s "sechub-pds-wrapper-prepare-$PREPARE_WRAPPER_VERSION.jar" "$TOOL_FOLDER/wrapper-prepare.jar"

# Set workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER "$USER"
