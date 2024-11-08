# SPDX-License-Identifier: MIT

#-------------------
# Global Variables
#-------------------

# The image argument needs to be placed on top
ARG BASE_IMAGE

# Build args
ARG PDS_VERSION
ARG BUILD_TYPE
ARG GO="go1.21.6.linux-amd64.tar.gz"

# possible values: temurin, openj9, openjdk
ARG JAVA_DISTRIBUTION="temurin"
# possible values: 17
ARG JAVA_VERSION="17"

# Artifact folder
ARG PDS_ARTIFACT_FOLDER="/artifacts"

#-------------------
# Builder Build
#-------------------

FROM ${BASE_IMAGE} AS builder-build

# Build args
ARG GO
ARG PDS_ARTIFACT_FOLDER
ARG JAVA_VERSION
ARG TAG
ARG BRANCH

ARG BUILD_FOLDER="/build"
ARG GIT_URL="https://github.com/mercedes-benz/sechub.git"

ENV DOWNLOAD_FOLDER="/downloads"
ENV PATH="/usr/local/go/bin:$PATH"

RUN mkdir --parent "$PDS_ARTIFACT_FOLDER" "$DOWNLOAD_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get upgrade --assume-yes --quiet && \
    apt-get install --quiet --assume-yes git w3m wget && \
    apt-get clean

# Install Go
RUN cd "$DOWNLOAD_FOLDER" && \
    # Get checksum from Go download site
    GO_CHECKSUM=`w3m https://go.dev/dl/ | grep "$GO" | tail -1 | awk '{print $6}'` && \
    # create checksum file
    echo "$GO_CHECKSUM $GO" > "$GO.sha256sum" && \
    # download Go
    wget --no-verbose https://go.dev/dl/"${GO}" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check "$GO.sha256sum" && \
    # extract Go
    tar --extract --file "$GO" --directory /usr/local/ && \
    # remove go tar.gz
    rm "$GO"

COPY --chmod=755 install-java/debian "$DOWNLOAD_FOLDER/install-java/"

# Install Java
RUN cd "$DOWNLOAD_FOLDER/install-java/" && \
    ./install-java.sh "$JAVA_DISTRIBUTION" "$JAVA_VERSION" jdk

# Copy clone script
COPY --chmod=755 clone.sh "$BUILD_FOLDER/clone.sh"

# Build SecHub
RUN mkdir --parent "$BUILD_FOLDER" && \
    cd "$BUILD_FOLDER" && \
    # execute the clone script
    ./clone.sh "$GIT_URL" "$BRANCH" "$TAG" && \
    cd "sechub" && \
    # Build PDS
    "./buildExecutables" && \
    cp sechub-pds/build/libs/sechub-pds-*.jar --target-directory "$PDS_ARTIFACT_FOLDER"

#-------------------
# Builder Download
#-------------------

FROM ${BASE_IMAGE} AS builder-download

ARG PDS_ARTIFACT_FOLDER
ARG PDS_VERSION

RUN mkdir --parent "$PDS_ARTIFACT_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install --assume-yes wget && \
    apt-get clean

# Download the PDS
RUN cd "$PDS_ARTIFACT_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar.sha256sum" && \
    # download pds
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check "sechub-pds-$PDS_VERSION.jar.sha256sum"

#-------------------
# Builder Copy Jar
#-------------------

FROM ${BASE_IMAGE} AS builder-copy

ARG PDS_ARTIFACT_FOLDER
ARG PDS_VERSION

RUN mkdir --parent "$PDS_ARTIFACT_FOLDER"

# Copy
COPY copy/sechub-pds-*.jar "$PDS_ARTIFACT_FOLDER"

#-------------------
# Builder
#-------------------

FROM builder-${BUILD_TYPE} as builder
RUN echo "build stage"

#-------------------
# PDS Server Image
#-------------------

FROM ${BASE_IMAGE} AS sechub

# Annotations according to the Open Containers Image Spec:
#  https://github.com/opencontainers/image-spec/blob/main/annotations.md

# Required by GitHub to link repository and image
LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub PDS Base Image"
LABEL org.opencontainers.image.description="The base image for the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

ARG JAVA_DISTRIBUTION
ARG JAVA_VERSION
ARG PDS_ARTIFACT_FOLDER
ARG PDS_VERSION

# env vars in container
ENV USER="pds"
ENV UID="2323"
ENV GID="${UID}"
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="$SHARED_VOLUMES/uploads"
ENV PDS_VERSION="${PDS_VERSION}"
ENV PDS_FOLDER="/pds"
ENV DOWNLOAD_FOLDER="$PDS_FOLDER/downloads"
ENV HELPER_FOLDER="$PDS_FOLDER/helper"
ENV MOCK_FOLDER="$PDS_FOLDER/mocks"
ENV SCRIPT_FOLDER="$PDS_FOLDER/scripts"
ENV TOOL_FOLDER="$PDS_FOLDER/tools"
ENV WORKSPACE="/workspace"

# non-root user
# using fixed group and user ids
RUN groupadd --gid "$GID" "$USER" && \
    useradd --uid "$UID" --gid "$GID" --no-log-init --create-home "$USER"

# Create folders & change owner of folders
RUN mkdir --parents "$PDS_FOLDER" "$SCRIPT_FOLDER" "$TOOL_FOLDER" "$WORKSPACE" "$DOWNLOAD_FOLDER" "$MOCK_FOLDER" "$SHARED_VOLUME_UPLOAD_DIR" "$HELPER_FOLDER" && \
    # Change owner and workspace and shared volumes folder
    # the only two folders pds really needs write access to
    chown --recursive "$USER:$USER" "$WORKSPACE" "$SHARED_VOLUMES"

COPY --from=builder "$PDS_ARTIFACT_FOLDER" "$PDS_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get upgrade --assume-yes --quiet && \
    apt-get install --assume-yes --quiet bind9-host curl netcat-openbsd tree vim-tiny && \
    apt-get clean

COPY --chmod=755 install-java/debian "$DOWNLOAD_FOLDER/install-java/"

# Install Java
RUN cd "$DOWNLOAD_FOLDER/install-java/" && \
    ./install-java.sh "$JAVA_DISTRIBUTION" "$JAVA_VERSION" jre

# Copy run script into the container
COPY run.sh /run.sh

# Copy the additional "hook" script into the container
COPY run_additional.sh /run_additional.sh

# Copy run script into the container
COPY helper/ "$HELPER_FOLDER"

# Set execute permissions for scripts
RUN chmod +x /run.sh /run_additional.sh

# Set permissions
RUN chown --recursive "$USER:$USER" "$PDS_FOLDER" "$SHARED_VOLUME_UPLOAD_DIR"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER"/pds-config.json

# Set workspace
WORKDIR "$PDS_FOLDER"

# Switch from root to non-root user
USER "$USER"

CMD ["/run.sh"]
