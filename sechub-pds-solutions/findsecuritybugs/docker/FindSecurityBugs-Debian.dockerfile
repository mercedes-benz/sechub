# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE

# Arguments
#   The FindSecurityBugs version to use. See https://github.com/find-sec-bugs/find-sec-bugs/releases
ARG FINDSECURITYBUGS_VERSION
#   The Spotbugs version to use. See https://github.com/spotbugs/spotbugs/releases
ARG SPOTBUGS_VERSION
#   Build type can be "build" or "download"
ARG BUILD_TYPE="build"
#   The base image of the builder
ARG BUILDER_BASE_IMAGE="debian:12-slim"
ARG ARTIFACT_FOLDER="/artifacts"


#-------------------
# Builder Download
#-------------------
# (downloads a released FindSecurityBugs bundle)

FROM ${BUILDER_BASE_IMAGE} AS builder-download

ARG ARTIFACT_FOLDER
ARG FINDSECURITYBUGS_VERSION

RUN mkdir -p "$ARTIFACT_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get -y install dos2unix unzip wget && \
    apt-get clean

# Download the Xray Wrapper (beware: this works only until version 1.12.0)
RUN cd "/tmp" && \
    # download pds
    wget --no-verbose "https://github.com/find-sec-bugs/find-sec-bugs/releases/download/version-$FINDSECURITYBUGS_VERSION/findsecbugs-cli-$FINDSECURITYBUGS_VERSION.zip" && \
    mv findsecbugs-cli-$FINDSECURITYBUGS_VERSION.zip "$ARTIFACT_FOLDER"


#-------------------
# Builder Build
#-------------------
# (downloads a released FindSecurityBugs bundle)

FROM ${BUILDER_BASE_IMAGE} AS builder-build

ARG ARTIFACT_FOLDER
ARG FINDSECURITYBUGS_VERSION
ARG SPOTBUGS_VERSION
ARG JAVA_DISTRIBUTION="temurin"
ARG JAVA_VERSION="17"

ENV DOWNLOAD_FOLDER="/downloads"
ENV BUILD_FOLDER="/build"

RUN mkdir -p "$ARTIFACT_FOLDER" "$DOWNLOAD_FOLDER" "$BUILD_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get -y install dos2unix git gradle unzip && \
    apt-get clean

COPY --chmod=755 install-java/debian "$DOWNLOAD_FOLDER/install-java/"

# Install Java
RUN cd "$DOWNLOAD_FOLDER/install-java/" && \
    ./install-java.sh "$JAVA_DISTRIBUTION" "$JAVA_VERSION" jdk

# Copy clone script
COPY --chmod=755 clone.sh "$BUILD_FOLDER/clone.sh"

RUN cd "$BUILD_FOLDER" && \
    ./clone.sh https://github.com/find-sec-bugs/find-sec-bugs.git master version-$FINDSECURITYBUGS_VERSION && \
    cd "find-sec-bugs/cli/" && \
    echo "fsbVersion=$FINDSECURITYBUGS_VERSION" > gradle.properties && \
    echo "spotbugsVersion=$SPOTBUGS_VERSION" >> gradle.properties && \
    gradle packageCli && \
    mv findsecbugs-cli-$FINDSECURITYBUGS_VERSION.zip "$ARTIFACT_FOLDER"


#-------------------
# Builder
#-------------------

FROM builder-${BUILD_TYPE} as builder

ARG ARTIFACT_FOLDER
ARG FINDSECURITYBUGS_VERSION
RUN echo "build stage - unpacking zip" && \
    cd "$ARTIFACT_FOLDER" && \
    unzip -q "findsecbugs-cli-$FINDSECURITYBUGS_VERSION.zip" && \
    dos2unix "$ARTIFACT_FOLDER/findsecbugs.sh" && \
    chmod +x "$ARTIFACT_FOLDER/findsecbugs.sh" && \
    rm -f "findsecbugs-cli-$FINDSECURITYBUGS_VERSION.zip"


#------------------------------
# PDS + FindSecurityBugs Image
#------------------------------
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub FindSecurityBugs+PDS Image"
LABEL org.opencontainers.image.description="A container which combines FindSecurityBugs with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

# Arguments
ARG ARTIFACT_FOLDER
ARG FINDSECURITYBUGS_VERSION

# Environment variables in container
ENV FINDSECURITYBUGS_VERSION="${FINDSECURITYBUGS_VERSION}"

USER root

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade  && \
    apt-get --assume-yes install libxml2-utils temurin-21-jre && \
    apt-get --assume-yes clean

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER/pds-config.json"

# Copy findsecuritybugs script into container
COPY findsecbugs_sechub.sh "$TOOL_FOLDER/findsecbugs_sechub.sh"
RUN chmod +x "$TOOL_FOLDER/findsecbugs_sechub.sh"

# Copy scripts
COPY scripts "$SCRIPT_FOLDER"
RUN chmod +x "$SCRIPT_FOLDER"/*.sh

# Mock folder
COPY mocks "$MOCK_FOLDER"

# Install FindSecurityBugs
# Copy artifacts from builder
COPY --from=builder "$ARTIFACT_FOLDER" "$TOOL_FOLDER"

# Set workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER "$USER"
