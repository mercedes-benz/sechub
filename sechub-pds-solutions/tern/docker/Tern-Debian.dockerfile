# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Tern+PDS Image"
LABEL org.opencontainers.image.description="A container which combines Tern with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

ARG TERN_VERSION="2.12.1"
ARG SCANCODE_VERSION="32.0.4"

# execute commands as root
USER root

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get --quiet update && \
    apt-get --quiet --assume-yes upgrade && \
    apt-get --quiet --assume-yes install attr bzip2 git jq libgomp1 libpopt0 libxml2-dev libxslt1-dev procps python3 python3-distutils python3-pip python-dev skopeo tar wget xz-utils zlib1g && \
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
