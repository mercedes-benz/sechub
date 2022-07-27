# SPDX-License-Identifier: MIT

#-------------------
# Global Variables
#-------------------

# The image argument needs to be placed on top
ARG BASE_IMAGE

# Build args
ARG PDS_VERSION
ARG BUILD_TYPE="download"
ARG PDS_ARTIFACT_FOLDER="/artifacts"
ARG GO="go1.18.3.linux-amd64.tar.gz"

# possible values are 11, 17
ARG JAVA_VERSION="11"

#-------------------
# Builder Build
#-------------------

FROM ${BASE_IMAGE} AS builder-build

# Build args
ARG GO
ARG PDS_ARTIFACT_FOLDER
ARG JAVA_VERSION

ARG BUILD_FOLDER="/build"
ARG GIT_URL="https://github.com/mercedes-benz/sechub.git"
ARG TAG=""

ENV DOWNLOAD_FOLDER="/downloads"
ENV PATH="/usr/local/go/bin:$PATH"

RUN mkdir --parent "$PDS_ARTIFACT_FOLDER" "$DOWNLOAD_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install --quiet --assume-yes wget w3m git "openjdk-$JAVA_VERSION-jdk-headless" && \
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

# Build SecHub
RUN mkdir --parent "$BUILD_FOLDER" && \
    cd "$BUILD_FOLDER" && \
    git clone "$GIT_URL" && \
    cd "sechub" && \
    "./buildExecutables" && \
    if [[ -n "$TAG" ]]; then git checkout tags/"$TAG" -b "$TAG"; fi && \
    cp "sechub-server/build/libs/sechub-server-0.0.0.jar" --target-directory "$PDS_ARTIFACT_FOLDER"

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
COPY copy/sechub-server-*.jar "$PDS_ARTIFACT_FOLDER"

#-------------------
# Builder
#-------------------

FROM builder-${BUILD_TYPE} as builder
RUN echo "build stage"

#-------------------
# PDS Server Image
#-------------------

FROM ${BASE_IMAGE} AS sechub

LABEL maintainer="SecHub FOSS Team"

ARG PDS_ARTIFACT_FOLDER
ARG JAVA_VERSION
ARG PDS_VERSION

# env vars in container
ENV USER="pds"
ENV UID="2323"
ENV GID="${UID}"
ENV SHARED_VOLUME_UPLOAD_DIR="/shared_volumes/uploads"
ENV PDS_VERSION="${PDS_VERSION}"

# arg vars in container
ARG PDS_FOLDER="/pds"
ARG SCRIPT_FOLDER="/scripts"

# non-root user
# using fixed group and user ids
RUN groupadd --gid "$GID" "$USER" && \
    useradd --uid "$UID" --gid "$GID" --no-log-init --create-home "$USER"

RUN mkdir --parent "$PDS_FOLDER" "$SHARED_VOLUME_UPLOAD_DIR"
COPY --from=builder "$PDS_ARTIFACT_FOLDER" "$PDS_FOLDER"

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
RUN chown --recursive "$USER:$USER" "$PDS_FOLDER" "$SHARED_VOLUME_UPLOAD_DIR"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER"/pds-config.json

# Set workspace
WORKDIR "$PDS_FOLDER"

# Switch from root to non-root user
USER "$USER"

CMD ["/run.sh"]
