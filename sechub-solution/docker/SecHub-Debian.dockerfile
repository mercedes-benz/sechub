# SPDX-License-Identifier: MIT

#-------------------
# Global Variables
#-------------------

# The image argument needs to be placed on top
ARG BASE_IMAGE

# Build args
ARG BUILD_TYPE="download"

ARG SECHUB_VERSION="0.35.2"
ARG GO="go1.19.3.linux-amd64.tar.gz"
ARG TAG=""
ARG BRANCH=""

# possible values: temurin, openj9, openjdk
ARG JAVA_DISTRIBUTION="openjdk"

# possible values are 11, 17
ARG JAVA_VERSION="11"

# Artifact folder
ARG SECHUB_ARTIFACT_FOLDER="/artifacts"

#-------------------
# Builder Build
#-------------------

FROM ${BASE_IMAGE} AS builder-build

# Build args
ARG GO
ARG SECHUB_ARTIFACT_FOLDER
ARG JAVA_VERSION
ARG JAVA_DISTRIBUTION
ARG TAG
ARG BRANCH

ARG BUILD_FOLDER="/build"
ARG GIT_URL="https://github.com/mercedes-benz/sechub.git"

ENV DOWNLOAD_FOLDER="/downloads"
ENV PATH="/usr/local/go/bin:$PATH"

RUN echo "Builder: Build"

RUN mkdir --parent "$SECHUB_ARTIFACT_FOLDER" "$DOWNLOAD_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install --quiet --assume-yes wget w3m git && \
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

COPY --chmod=755 install-java/ "$DOWNLOAD_FOLDER/install-java/"

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
    # Java version
    java --version && \
    # Build SecHub
    "./buildExecutables" && \
    cp sechub-server/build/libs/sechub-server-*.jar --target-directory "$SECHUB_ARTIFACT_FOLDER"

#-------------------
# Builder Download
#-------------------

FROM ${BASE_IMAGE} AS builder-download

ARG SECHUB_ARTIFACT_FOLDER
ARG SECHUB_VERSION

RUN echo "Builder: Download"

RUN mkdir --parent "$SECHUB_ARTIFACT_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install --assume-yes wget && \
    apt-get clean

# Download the SecHub server
RUN cd "$SECHUB_ARTIFACT_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$SECHUB_VERSION-server/sechub-server-$SECHUB_VERSION.jar.sha256sum" && \
    # download pds
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$SECHUB_VERSION-server/sechub-server-$SECHUB_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check "sechub-server-$SECHUB_VERSION.jar.sha256sum"

#-------------------
# Builder Copy Jar
#-------------------

FROM ${BASE_IMAGE} AS builder-copy

ARG SECHUB_ARTIFACT_FOLDER
ARG SECHUB_VERSION

RUN echo "Builder: Copy"

RUN mkdir --parent "$SECHUB_ARTIFACT_FOLDER"

# Copy
COPY copy/sechub-server-*.jar "$SECHUB_ARTIFACT_FOLDER"

#-------------------
# Builder
#-------------------

FROM builder-${BUILD_TYPE} as builder

#-------------------
# SecHub Server Image
#-------------------

FROM ${BASE_IMAGE} AS sechub

LABEL maintainer="SecHub FOSS Team"

ARG SECHUB_ARTIFACT_FOLDER
ARG JAVA_DISTRIBUTION
ARG JAVA_VERSION

# env vars in container
ENV USER="sechub"
ENV UID="7474"
ENV GID="${UID}"
ENV SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR="/shared_volumes/uploads"

ARG SECHUB_FOLDER="/sechub"

# non-root user
# using fixed group and user ids
RUN groupadd --gid "$GID" "$USER" && \
    useradd --uid "$UID" --gid "$GID" --no-log-init --create-home "$USER"

RUN mkdir --parent "$SECHUB_FOLDER" "$SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR"
COPY --from=builder "$SECHUB_ARTIFACT_FOLDER" "$SECHUB_FOLDER"

# Upgrade system
RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get upgrade --assume-yes --quiet && \
    apt-get clean

COPY --chmod=755 install-java/ "$SECHUB_FOLDER/install-java/"

# Install Java
RUN cd "$SECHUB_FOLDER/install-java/" && \
    ./install-java.sh "$JAVA_DISTRIBUTION" "$JAVA_VERSION" jre

# Copy run script into container
COPY run.sh /run.sh

# Set execute permissions for scripts
RUN chmod +x /run.sh

# Set permissions
RUN chown --recursive "$USER:$USER" "$SECHUB_FOLDER" "$SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR"

# Set workspace
WORKDIR "$SECHUB_FOLDER"

# Switch from root to non-root user
USER "$USER"

CMD ["/run.sh"]
