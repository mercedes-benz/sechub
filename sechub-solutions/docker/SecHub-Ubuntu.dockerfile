# SPDX-License-Identifier: MIT

#-------------------
# Global Variables
#-------------------

# The image argument needs to be placed on top
ARG BASE_IMAGE

ARG BUILD_TYPE="download"
ARG SECHUB_ARTIFACT_FOLDER="/artifacts"
ARG SECHUB_VERSION="0.29.0"

# possible values are 8, 11, 17
ARG JAVA_VERSION="8"

#-------------------
# Builder Build
#-------------------

FROM ${BASE_IMAGE} AS builder-build

ARG SECHUB_ARTIFACT_FOLDER
ARG JAVA_VERSION

ARG BUILD_FOLDER="/build"
ARG GIT_URL="https://github.com/mercedes-benz/sechub.git"
ARG TAG=""

RUN mkdir --parent "$SECHUB_ARTIFACT_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install --quiet --assume-yes git golang "openjdk-$JAVA_VERSION-jdk-headless" && \
    apt-get clean

RUN mkdir --parent "$BUILD_FOLDER" && \
    cd "$BUILD_FOLDER" && \
    git clone "$GIT_URL" && \
    cd "sechub" && \
    "./buildExecutables" && \
    if [ -z "$TAG"]; then git checkout tags/"$TAG" -b "$TAG"; fi && \
    cp "sechub-server/build/libs/sechub-server-0.0.0.jar" --target-directory "$SECHUB_ARTIFACT_FOLDER"

#-------------------
# Builder Download
#-------------------

FROM ${BASE_IMAGE} AS builder-download

ARG SECHUB_ARTIFACT_FOLDER
ARG SECHUB_VERSION

RUN mkdir --parent "$SECHUB_ARTIFACT_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install --assume-yes wget && \
    apt-get clean

# Install the SecHub Product Delegation Server (PDS)
RUN cd "$SECHUB_ARTIFACT_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$SECHUB_VERSION-server/sechub-server-$SECHUB_VERSION.jar.sha256sum" && \
    # download pds
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$SECHUB_VERSION-server/sechub-server-$SECHUB_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check "sechub-server-$SECHUB_VERSION.jar.sha256sum"

#-------------------
# Builder
#-------------------

FROM builder-${BUILD_TYPE} as builder
RUN echo "build stage"

#-------------------
# SecHub Server Image
#-------------------

FROM ${BASE_IMAGE} AS sechub

LABEL maintainer="SecHub FOSS Team"

ARG SECHUB_ARTIFACT_FOLDER
ARG JAVA_VERSION

# env vars in container:
ENV USER="sechub"
ENV UID="7474"
ENV GID="${UID}"
ENV SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR="/shared_volume/uploads"

ARG SECHUB_FOLDER="/sechub"

# non-root user
# using fixed group and user ids
RUN groupadd --gid "$GID" "$USER" && \
    useradd --uid "$UID" --gid "$GID" --no-log-init --create-home "$USER"

RUN mkdir --parent "$SECHUB_FOLDER" "$SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR"
COPY --from=builder "$SECHUB_ARTIFACT_FOLDER" "$SECHUB_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get upgrade --assume-yes --quiet && \
    apt-get install --assume-yes --quiet "openjdk-$JAVA_VERSION-jre-headless" && \
    apt-get clean

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
