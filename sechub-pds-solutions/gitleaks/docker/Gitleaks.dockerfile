# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE

# Mandatory arguments
ARG GITLEAKS_VERSION
ARG SECRETVALIDATION_WRAPPER_VERSION

# Build type can be "copy" or "download"
ARG BUILD_TYPE="download"

# The base image of the builder
ARG BUILDER_BASE_IMAGE="debian:12-slim"
ARG ARTIFACT_FOLDER="/artifacts"


#-------------------
# Builder Download
#-------------------
# (downloads a released Secret-Validation Wrapper jar)

FROM ${BUILDER_BASE_IMAGE} AS builder-download

ARG ARTIFACT_FOLDER
ARG SECRETVALIDATION_WRAPPER_VERSION

RUN mkdir -p "$ARTIFACT_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install -y wget && \
    apt-get clean

# Download the Secret-Validation Wrapper
RUN cd "$ARTIFACT_FOLDER" && \
    # download wrapper jar
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$SECRETVALIDATION_WRAPPER_VERSION-secretvalidation-wrapper/sechub-wrapper-secretvalidation-$SECRETVALIDATION_WRAPPER_VERSION.jar" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$SECRETVALIDATION_WRAPPER_VERSION-secretvalidation-wrapper/sechub-wrapper-secretvalidation-$SECRETVALIDATION_WRAPPER_VERSION.jar.sha256sum" && \
    # verify the checksum
    sha256sum --check "sechub-wrapper-secretvalidation-$SECRETVALIDATION_WRAPPER_VERSION.jar.sha256sum"


#-------------------
# Builder Copy Jar
#-------------------
# (copies the Secret-Validation Wrapper jar from local subdirectory "copy")

FROM ${BUILDER_BASE_IMAGE} AS builder-copy

ARG ARTIFACT_FOLDER
ARG SECRETVALIDATION_WRAPPER_VERSION

RUN mkdir -p "$ARTIFACT_FOLDER"

# Copy
COPY copy/sechub-wrapper-secretvalidation-*.jar "$ARTIFACT_FOLDER"


#-------------------
# Builder
#-------------------

FROM builder-${BUILD_TYPE} AS builder
RUN echo "build stage"


#-------------------
# PDS + Gitleaks Image
#-------------------

FROM ${BASE_IMAGE}

ARG ARTIFACT_FOLDER
ARG GITLEAKS_VERSION
ARG SECRETVALIDATION_WRAPPER_VERSION

ENV GITLEAKS_VERSION="${GITLEAKS_VERSION}"

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Gitleaks extended + PDS Image"
LABEL org.opencontainers.image.description="A container which combines Gitleaks and Secret-Validation with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

USER root

# Copy run_additional script
COPY --chmod=0755 run_additional.sh /run_additional.sh

# Copy mock folder
COPY mocks "$MOCK_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER"/pds-config.json

# Copy scripts
COPY scripts "$SCRIPT_FOLDER"
RUN chmod -R +x "$SCRIPT_FOLDER"

# Copy Secret-Validation Wrapper jar from builder
COPY --from=builder "$ARTIFACT_FOLDER" "$TOOL_FOLDER"
RUN cd "$TOOL_FOLDER" && \
    ln -s sechub-wrapper-secretvalidation-*.jar sechub-wrapper-secretvalidation.jar

# Copy custom rule file custom-gitleaks.toml
COPY custom-gitleaks.toml "$TOOL_FOLDER"

# Copy default config file for Secret-Validation Wrapper
COPY sechub-wrapper-secretvalidation-config.json "$TOOL_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install -y -q git openssh-client wget && \
    apt-get clean

# Download Gitleaks + cleanup
RUN cd "$DOWNLOAD_FOLDER" && \
    # Download Gitleaks
    wget "https://github.com/gitleaks/gitleaks/releases/download/v${GITLEAKS_VERSION}/gitleaks_${GITLEAKS_VERSION}_checksums.txt" && \
    wget "https://github.com/gitleaks/gitleaks/releases/download/v${GITLEAKS_VERSION}/gitleaks_${GITLEAKS_VERSION}_linux_x64.tar.gz" && \
    # Verify checksum
    sha256sum --check --ignore-missing "gitleaks_${GITLEAKS_VERSION}_checksums.txt" && \
    # Extract Gitleaks into $TOOL_FOLDER/gitleaks
    mkdir "$TOOL_FOLDER"/gitleaks && \
    tar --extract --gunzip --file="gitleaks_${GITLEAKS_VERSION}_linux_x64.tar.gz" --directory="$TOOL_FOLDER/gitleaks" && \
    # Cleanup download directory
    rm -rf "$DOWNLOAD_FOLDER"/*

# Set workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER "$USER"
