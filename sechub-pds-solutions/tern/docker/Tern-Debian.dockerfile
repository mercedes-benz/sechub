# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Tern+PDS Image"
LABEL org.opencontainers.image.description="A container which combines Tern with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

ARG TERN_VERSION="2.11.0"
ARG SCANCODE_VERSION="31.2.4"

# execute commands as root
USER root

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get --quiet update && \
    apt-get --quiet --assume-yes upgrade && \
    apt-get --quiet --assume-yes install wget attr jq skopeo python3-pip procps tar python3 python3-distutils python-dev git bzip2 xz-utils zlib1g libxml2-dev libxslt1-dev libgomp1 libpopt0 && \
    apt-get --quiet --assume-yes clean

# Install Tern
RUN pip install --no-warn-script-location "tern==${TERN_VERSION}" "scancode-toolkit[full]==${SCANCODE_VERSION}"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER"/pds-config.json

# Copy scripts
COPY scripts "$SCRIPT_FOLDER"
RUN chmod --recursive +x "$SCRIPT_FOLDER"

# Copy mock folder
COPY mocks "$MOCK_FOLDER"