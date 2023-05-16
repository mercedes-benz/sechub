# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Multiple Tools + PDS Image"
LABEL org.opencontainers.image.description="A container which combines Multiple Tools with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

user root

# Copy PDS configfile
COPY pds-config.json "/$PDS_FOLDER/pds-config.json"

# Copy scripts
COPY scripts "$SCRIPT_FOLDER"
RUN chmod --recursive +x "$SCRIPT_FOLDER"

# Mock folder
COPY mocks "$MOCK_FOLDER"

# Update image and install dependencies
RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade  && \
    apt-get --assume-yes install sed wget openjdk-11-jre-headless pip && \
    apt-get --assume-yes clean

# Install Flawfinder, Bandit, njsscan and mobsfscan
COPY packages.txt $TOOL_FOLDER/packages.txt
RUN pip install -r $TOOL_FOLDER/packages.txt

# Create the PDS workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER "$USER"
