# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub INFRALIGHT + PDS Image"
LABEL org.opencontainers.image.description="A container which combines INFRALIGHT with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

USER root

# Copy mock folders
COPY mocks "$MOCK_FOLDER"

# Copy scripts
COPY scripts "$SCRIPT_FOLDER"
RUN chmod --recursive +x "$SCRIPT_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER/pds-config.json"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get upgrade --assume-yes && \
    apt-get install --assume-yes firefox-esr wget nmap jq && \
    apt-get clean

# Switch from root to non-root user
USER "$USER"

# Switch to workspace folder
WORKDIR "$WORKSPACE"
