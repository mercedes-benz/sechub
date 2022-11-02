# SPDX-License-Identifier: MIT

ARG BASE_IMAGE
FROM ${BASE_IMAGE}

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Scancode-Toolkit+PDS Image"
LABEL org.opencontainers.image.description="A container which combines Scancode-Toolkit with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

# Build args
ARG SPDX_TOOL_VERISON="1.1.2"
ARG SPDX_TOOL_CHECKSUM="4a2f1a2f3a12b96fc13675e78871a33dc12f6e44c7dbdcda2e5ea92f994615e8  tools-java-1.1.2-jar-with-dependencies.jar"

# Environment variables in container
ENV SCANCODE_VERSION="${SCANCODE_VERSION}"
ENV SPDX_TOOL_VERISON="${SPDX_TOOL_VERISON}"

USER root

# Copy mock folder
COPY mocks "$MOCK_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER"/pds-config.json

# Update image and install dependencies
RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade  && \
    apt-get --assume-yes install wget \
                                 tar \
                                 tree \
                                 procps \
                                 python3 \
                                 python3-distutils \
                                 python-dev \
                                 bzip2 \
                                 xz-utils \
                                 zlib1g \
                                 libxml2-dev \
                                 libxslt1-dev \
                                 libgomp1 \
                                 libpopt0 \
                                 python3-pip && \
    apt-get --assume-yes clean

# Install Scancode
COPY packages.txt "${TOOL_FOLDER}/packages.txt"
RUN pip install -r "${TOOL_FOLDER}/packages.txt"

# Install SPDX Tools Java converter
RUN cd "$TOOL_FOLDER" && \
    # download SPDX Tools Java 
    wget --no-verbose "https://repo1.maven.org/maven2/org/spdx/tools-java/${SPDX_TOOL_VERISON}/tools-java-${SPDX_TOOL_VERISON}-jar-with-dependencies.jar" && \
    # create checksum file
    echo "$SPDX_TOOL_CHECKSUM" > checksum-spdx-tool.sha256sum && \
    # check against checksum file
    sha256sum -c checksum-spdx-tool.sha256sum

# Copy scripts
COPY scripts $SCRIPT_FOLDER
RUN chmod --recursive +x $SCRIPT_FOLDER

# Patch
COPY pool.py /usr/local/lib/python3.9/dist-packages/scancode/pool.py

# Set workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER "$USER"
