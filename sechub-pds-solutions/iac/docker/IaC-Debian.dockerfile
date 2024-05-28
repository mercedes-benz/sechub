# SPDX-License-Identifier: MIT

#-------------------
# Global Variables
#-------------------

# The image argument needs to be placed on top
ARG BASE_IMAGE
ARG KICS_VERSION

# Build args
ARG GO="go1.21.6.linux-amd64.tar.gz"

# Artifact folder
ARG PDS_ARTIFACT_FOLDER="/artifacts"

#-------------------
# Builder
#-------------------

FROM ${BASE_IMAGE} AS builder

# Build args
ARG GO
ARG PDS_ARTIFACT_FOLDER
ARG BUILD_FOLDER="/build"
ARG KICS_VERSION="2.0.1"

# Environment variables in container
ENV DOWNLOAD_FOLDER="/downloads"
ENV PATH="/usr/local/go/bin:$PATH"

ENV KICS_VERSION="${KICS_VERSION}"

USER root

RUN mkdir --parent "$PDS_ARTIFACT_FOLDER" "$DOWNLOAD_FOLDER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install --quiet --assume-yes wget w3m git unzip && \
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

# Build Kics
RUN mkdir --parent "$BUILD_FOLDER" && \
    cd "$BUILD_FOLDER" && \
    # Download and extract Kics
    wget --no-verbose https://github.com/Checkmarx/kics/archive/refs/tags/v${KICS_VERSION}.zip && \
    unzip -q v${KICS_VERSION}.zip && \
    mv kics-${KICS_VERSION} kics && \
    cd "kics" && \
    # Downloads Go packages
    go mod vendor && \
    # Build kics
    CGO_ENABLED=0 go build -ldflags "-s -w -X github.com/Checkmarx/kics/internal/constants.Version=${KICS_VERSION}" -a -installsuffix cgo -o bin/kics cmd/console/main.go && \
    # copy kics binary
    mkdir --parents "$PDS_ARTIFACT_FOLDER/kics/" && \
    cp bin/kics --target-directory "$PDS_ARTIFACT_FOLDER/kics/" && \
    # copy assets
    cp --recursive assets --target-directory "$PDS_ARTIFACT_FOLDER/kics/"

#-------------------
# PDS Image
#-------------------

FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub IaC+PDS Image"
LABEL org.opencontainers.image.description="A container which combines Infrastructure as Code tools with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

ARG PDS_ARTIFACT_FOLDER

ENV PATH "$TOOL_FOLDER/kics:$PATH"

USER root

COPY --from=builder "$PDS_ARTIFACT_FOLDER" "$TOOL_FOLDER"

# Copy mock folder
COPY mocks "$MOCK_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER/pds-config.json"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade && \
    apt-get --assume-yes install w3m wget jq && \
    apt-get --assume-yes clean

# Copy scripts
COPY scripts $SCRIPT_FOLDER
RUN chmod --recursive +x $SCRIPT_FOLDER

# Set workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER "$USER"
