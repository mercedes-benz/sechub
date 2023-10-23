# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Xray+PDS Image"
LABEL org.opencontainers.image.description="A container which combines Xray Wrapper with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

USER root

# Build Args
ARG XRAY_WRAPPER_VERSION="0.0.0"

# Copy mock folder
COPY mocks "$MOCK_FOLDER"

# Copy scripts
COPY scripts "$SCRIPT_FOLDER"
RUN chmod --recursive +x "$SCRIPT_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER/pds-config.json"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade && \
    apt-get --assume-yes install openjdk-17-jre wget skopeo jq && \
    apt-get --assume-yes clean

# TODO: Install SecHub XRAY wrapper from github
        #RUN cd "$TOOL_FOLDER" && \
        #    # download checksum file
        #    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$Xlink" && \
        #    # download wrapper jar
        #    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$link" && \
        #    # verify that the checksum and the checksum of the file are same
        #    sha256sum --check sechub-pds-wrapperxray-$XRAY_WRAPPER_VERSION.jar.sha256sum && \
        #    ln -s sechub-pds-wrapperxray-$XRAY_WRAPPER_VERSION.jar wrapperxray.jar

# workaround until release
COPY sechub-pds-wrapperxray-$XRAY_WRAPPER_VERSION.jar "$TOOL_FOLDER""/sechub-pds-wrapperxray-$XRAY_WRAPPER_VERSION.jar"
RUN ln -s "$TOOL_FOLDER""/sechub-pds-wrapperxray-$XRAY_WRAPPER_VERSION.jar" "$TOOL_FOLDER""/wrapperxray.jar"

# Set workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER "$USER"
