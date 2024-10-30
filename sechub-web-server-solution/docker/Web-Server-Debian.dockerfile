# SPDX-License-Identifier: MIT

#-------------------
# Global Variables
#-------------------

# The image argument needs to be placed on top
ARG BASE_IMAGE

# Build args
ARG WEB_SERVER_VERSION
ARG BUILD_TYPE

# possible values: temurin, openj9, openjdk
ARG JAVA_DISTRIBUTION="temurin"
# possible values: 17
ARG JAVA_VERSION="17"

# Artifact folder
ARG WEB_SERVER_ARTIFACT_FOLDER="/artifacts"

#-------------------
# Builder Build
#-------------------

FROM ${BASE_IMAGE} AS builder-build

# Build args
ARG JAVA_DISTRIBUTION
ARG JAVA_VERSION
ARG TAG
ARG BRANCH
ARG WEB_SERVER_ARTIFACT_FOLDER

ARG BUILD_FOLDER="/build"
ARG GIT_URL="https://github.com/mercedes-benz/sechub.git"

ENV DOWNLOAD_FOLDER="/downloads"

RUN mkdir --parent "$WEB_SERVER_ARTIFACT_FOLDER" "$DOWNLOAD_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get upgrade --assume-yes --quiet && \
    apt-get install --assume-yes --quiet git wget && \
    apt-get clean

COPY --chmod=755 install-java/debian "$DOWNLOAD_FOLDER/install-java/"

# Install Java
RUN cd "$DOWNLOAD_FOLDER/install-java/" && \
    ./install-java.sh "$JAVA_DISTRIBUTION" "$JAVA_VERSION" jdk

# Copy clone script
COPY --chmod=755 clone.sh "$BUILD_FOLDER/clone.sh"

# Clone SecHub repo and build sechub-web-server jar file
RUN mkdir --parent "$BUILD_FOLDER" && \
    cd "$BUILD_FOLDER" && \
    ./clone.sh "$GIT_URL" "$BRANCH" "$TAG" && \
    cd "sechub" && \
    ./gradlew ensureLocalhostCertificate :sechub-api-java:build :sechub-web-server:build -Dsechub.build.stage=api-necessary --console=plain && \
    cd sechub-web-server/build/libs/ && \
    rm -f *-javadoc.jar *-plain.jar *-sources.jar && \
    cp sechub-web-server-*.jar --target-directory "$WEB_SERVER_ARTIFACT_FOLDER"

#-------------------
# Builder Download
#-------------------

FROM ${BASE_IMAGE} AS builder-download

ARG WEB_SERVER_ARTIFACT_FOLDER
ARG WEB_SERVER_VERSION

RUN mkdir --parent "$WEB_SERVER_ARTIFACT_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install --assume-yes wget && \
    apt-get clean

# Download the SecHub web-server jar file
RUN cd "$WEB_SERVER_ARTIFACT_FOLDER" && \
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$WEB_SERVER_VERSION-web-server/sechub-web-server-$WEB_SERVER_VERSION.jar.sha256sum" && \
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$WEB_SERVER_VERSION-web-server/sechub-web-server-$WEB_SERVER_VERSION.jar" && \
    sha256sum --check "sechub-web-server-$WEB_SERVER_VERSION.jar.sha256sum"

#-------------------
# Builder Copy Jar
#-------------------

FROM ${BASE_IMAGE} AS builder-copy

ARG WEB_SERVER_ARTIFACT_FOLDER
ARG WEB_SERVER_VERSION

RUN mkdir --parent "$WEB_SERVER_ARTIFACT_FOLDER"

COPY copy/sechub-web-server-*.jar "$WEB_SERVER_ARTIFACT_FOLDER"

#-------------------
# Builder
#-------------------

FROM builder-${BUILD_TYPE} as builder
RUN echo "build stage"

#-------------------
# Web Server Image
#-------------------

FROM ${BASE_IMAGE} AS web-server

# Annotations according to the Open Containers Image Spec:
# https://github.com/opencontainers/image-spec/blob/main/annotations.md

# Required by GitHub to link repository and image
LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Web Server Image"
LABEL org.opencontainers.image.description="The SecHub Web Server image"
LABEL maintainer="SecHub FOSS Team"

ARG WEB_SERVER_ARTIFACT_FOLDER
ARG JAVA_DISTRIBUTION
ARG JAVA_VERSION
ARG WEB_SERVER_VERSION

# env vars in container
ENV USER="web-server"
ENV UID="4242"
ENV GID="${UID}"
ENV WEB_SERVER_VERSION="${WEB_SERVER_VERSION}"
ENV WEB_SERVER_FOLDER="/sechub-web-server"

# non-root user
# using fixed group and user ids
RUN groupadd --gid "$GID" "$USER" && \
    useradd --uid "$UID" --gid "$GID" --no-log-init --create-home "$USER"

# Create folders
RUN mkdir --parents "$WEB_SERVER_FOLDER"

COPY --from=builder "$WEB_SERVER_ARTIFACT_FOLDER" "$WEB_SERVER_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get upgrade --assume-yes --quiet && \
    apt-get install --assume-yes --quiet tree unzip && \
    apt-get clean

COPY --chmod=755 install-java/debian "$DOWNLOAD_FOLDER/install-java/"

# Install Java
RUN cd "$DOWNLOAD_FOLDER/install-java/" && \
    ./install-java.sh "$JAVA_DISTRIBUTION" "$JAVA_VERSION" jre

# Copy run script into the container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# Set permissions
RUN chown --recursive "$USER:$USER" "$WEB_SERVER_FOLDER"

# Set workspace
WORKDIR "$WEB_SERVER_FOLDER"

# Switch from root to non-root user
USER "$USER"

CMD ["/run.sh"]
