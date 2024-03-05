# SPDX-License-Identifier: MIT

#-------------------
# Global Variables
#-------------------

# The image argument needs to be placed on top
ARG BASE_IMAGE

# Build args
ARG WEBUI_VERSION
ARG BUILD_TYPE

# possible values: 17
ARG JAVA_VERSION="17"

# Artifact folder
ARG WEBUI_ARTIFACT_FOLDER="/artifacts"

#-------------------
# Builder Build
#-------------------

FROM ${BASE_IMAGE} AS builder-build

# Build args
ARG JAVA_VERSION
ARG TAG
ARG BRANCH
ARG WEBUI_ARTIFACT_FOLDER

ARG BUILD_FOLDER="/build"
ARG GIT_URL="https://github.com/mercedes-benz/sechub.git"

ENV DOWNLOAD_FOLDER="/downloads"

RUN mkdir --parent "$WEBUI_ARTIFACT_FOLDER" "$DOWNLOAD_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get upgrade --assume-yes --quiet && \
    apt-get install --quiet --assume-yes wget w3m git "openjdk-$JAVA_VERSION-jdk-headless" && \
    apt-get clean

# Copy clone script
COPY --chmod=755 clone.sh "$BUILD_FOLDER/clone.sh"

# Clone SecHub repo and build SecHub WebUI jar file
RUN mkdir --parent "$BUILD_FOLDER" && \
    cd "$BUILD_FOLDER" && \
    ./clone.sh "$GIT_URL" "$BRANCH" "$TAG" && \
    cd "sechub" && \
    ./gradlew :sechub-api-java:build :sechub-webui:build -Dsechub.build.stage=api-necessary --console=plain && \
    cd sechub-webui/build/libs/ && \
    rm -f *-javadoc.jar *-plain.jar *-sources.jar && \
    cp sechub-webui-*.jar --target-directory "$WEBUI_ARTIFACT_FOLDER"

#-------------------
# Builder Download
#-------------------

FROM ${BASE_IMAGE} AS builder-download

ARG WEBUI_ARTIFACT_FOLDER
ARG WEBUI_VERSION

RUN mkdir --parent "$WEBUI_ARTIFACT_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install --assume-yes wget && \
    apt-get clean

# Download the SecHub WebUI jar file
RUN cd "$WEBUI_ARTIFACT_FOLDER" && \
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$WEBUI_VERSION-pds/sechub-pds-$WEBUI_VERSION.jar.sha256sum" && \
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$WEBUI_VERSION-pds/sechub-pds-$WEBUI_VERSION.jar" && \
    sha256sum --check "sechub-pds-$WEBUI_VERSION.jar.sha256sum"

#-------------------
# Builder Copy Jar
#-------------------

FROM ${BASE_IMAGE} AS builder-copy

ARG WEBUI_ARTIFACT_FOLDER
ARG WEBUI_VERSION

RUN mkdir --parent "$WEBUI_ARTIFACT_FOLDER"

COPY copy/sechub-webui-*.jar "$WEBUI_ARTIFACT_FOLDER"

#-------------------
# Builder
#-------------------

FROM builder-${BUILD_TYPE} as builder
RUN echo "build stage"

#-------------------
# WebUI Server Image
#-------------------

FROM ${BASE_IMAGE} AS webui

# Annotations according to the Open Containers Image Spec:
# https://github.com/opencontainers/image-spec/blob/main/annotations.md

# Required by GitHub to link repository and image
LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub WebUI Image"
LABEL org.opencontainers.image.description="The SecHub WebUI image"
LABEL maintainer="SecHub FOSS Team"

ARG WEBUI_ARTIFACT_FOLDER
ARG JAVA_VERSION
ARG WEBUI_VERSION

# env vars in container
ENV USER="webui"
ENV UID="4242"
ENV GID="${UID}"
ENV WEBUI_VERSION="${WEBUI_VERSION}"
ENV WEBUI_FOLDER="/sechub-webui"
ENV HELPER_FOLDER="$WEBUI_FOLDER/helper"

# non-root user
# using fixed group and user ids
RUN groupadd --gid "$GID" "$USER" && \
    useradd --uid "$UID" --gid "$GID" --no-log-init --create-home "$USER"

# Create folders
RUN mkdir --parents "$WEBUI_FOLDER" "$HELPER_FOLDER"

COPY --from=builder "$WEBUI_ARTIFACT_FOLDER" "$WEBUI_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get upgrade --assume-yes --quiet && \
    apt-get install --assume-yes --quiet "openjdk-$JAVA_VERSION-jre-headless" tree && \
    apt-get clean

# Copy run script into the container
COPY run.sh /run.sh

# Copy run script into the container
COPY helper/ "$HELPER_FOLDER"

# Set execute permissions for scripts
RUN chmod +x /run.sh

# Set permissions
RUN chown --recursive "$USER:$USER" "$WEBUI_FOLDER"

# Set workspace
WORKDIR "$WEBUI_FOLDER"

# Switch from root to non-root user
USER "$USER"

CMD ["/run.sh"]
