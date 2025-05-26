# SPDX-License-Identifier: MIT

#-------------------
# Global Variables
#-------------------

# The image argument needs to be placed on top
ARG BASE_IMAGE

# Build args
ARG BUILD_TYPE="download"

ARG SECHUB_VERSION=""
ARG TAG=""
ARG BRANCH=""

# possible values: temurin, openj9, openjdk
ARG JAVA_DISTRIBUTION="temurin"

# possible values: 17
ARG JAVA_VERSION="17"

# Artifact folder
ARG SECHUB_ARTIFACT_FOLDER="/artifacts"

#-------------------
# Builder Build
#-------------------

FROM ${BASE_IMAGE} AS builder-build

# Build args
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

RUN apk update && \
    apk add --no-cache wget git bash go

COPY --chmod=755 install-java/alpine "$DOWNLOAD_FOLDER/install-java/"

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

RUN apk update && \
    apk add --no-cache wget

# Download the SecHub server
RUN cd "$SECHUB_ARTIFACT_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v${SECHUB_VERSION}-server/sechub-server-${SECHUB_VERSION}.jar.sha256sum" && \
    # download pds
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v${SECHUB_VERSION}-server/sechub-server-${SECHUB_VERSION}.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum -c "sechub-server-${SECHUB_VERSION}.jar.sha256sum"

#-------------------
# Builder Copy Jar
#-------------------

FROM ${BASE_IMAGE} AS builder-copy

ARG SECHUB_ARTIFACT_FOLDER

RUN echo "Builder: Copy"

RUN mkdir --parent "$SECHUB_ARTIFACT_FOLDER"

# Copy
COPY copy/sechub-server-*.jar "$SECHUB_ARTIFACT_FOLDER"

#-------------------
# Builder
#-------------------

FROM builder-${BUILD_TYPE} AS builder

#-------------------
# SecHub Server Image
#-------------------

FROM ${BASE_IMAGE} AS sechub

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Alpine Linux Image"
LABEL org.opencontainers.image.description="A container for SecHub based on Alpine Linux"
LABEL maintainer="SecHub FOSS Team"

ARG SECHUB_ARTIFACT_FOLDER
ARG JAVA_DISTRIBUTION
ARG JAVA_VERSION

# env vars in container
ENV USER="sechub"
ENV UID="7474"
ENV GID="${UID}"
ENV LANG="C.UTF-8"
ENV SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR="/shared_volumes/uploads"

ARG SECHUB_FOLDER="/sechub"

# non-root user
# using fixed group and user ids
RUN addgroup --gid "$GID" "$USER"
RUN adduser --uid "$UID" --ingroup "$USER" --disabled-password "$USER"

# Mounted secret files (like e.g. SSL certificates) go to $SECHUB_FOLDER/secrets. See deployment.yaml file.
RUN mkdir --parent "$SECHUB_FOLDER/secrets" "$SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR"
COPY --from=builder "$SECHUB_ARTIFACT_FOLDER" "$SECHUB_FOLDER"

COPY --chmod=755 install-java/alpine "$SECHUB_FOLDER/install-java/"

# Update packages
RUN apk update && \
    apk add --no-cache curl netcat-openbsd

# Install Java
RUN cd "$SECHUB_FOLDER/install-java/" && \
    ./install-java.sh "$JAVA_DISTRIBUTION" "$JAVA_VERSION" jre

# Copy run script into container and make it executable
COPY run.sh /run.sh
RUN chmod +x /run.sh

# Set permissions and remove unnecessary files
RUN chown --recursive "$USER:$USER" "$SECHUB_FOLDER" "$SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR" && \
    rm -rf /var/cache/apk/* "$SECHUB_FOLDER/install-java/"

# Set workspace
WORKDIR "$SECHUB_FOLDER"

# Switch from root to non-root user
USER "$USER"

CMD ["/run.sh"]
