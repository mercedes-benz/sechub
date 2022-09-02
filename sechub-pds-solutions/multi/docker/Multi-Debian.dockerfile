# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

LABEL maintainer="SecHub FOSS Team"

# Build args
ARG PDS_FOLDER="/pds"
ARG PDS_VERSION="0.31.0"
ARG SCRIPT_FOLDER="/scripts"
ARG WORKSPACE="/workspace"

# Environment variables in container
ENV DOWNLOAD_FOLDER="/downloads"
ENV MOCK_FOLDER="/mocks"
ENV PDS_VERSION="${PDS_VERSION}"
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="$SHARED_VOLUMES/uploads"
ENV TOOL_FOLDER="/tools"

# non-root user
# using fixed group and user ids
RUN groupadd --gid 2323 pds \
     && useradd --uid 2323 --no-log-init --create-home --gid pds pds

# Create tool, pds, shared volume and download folder
RUN  mkdir --parents "$SCRIPT_FOLDER" "$TOOL_FOLDER" "$DOWNLOAD_FOLDER" "$PDS_FOLDER" "$SHARED_VOLUME_UPLOAD_DIR" "$WORKSPACE" && \
    # Change owner and workspace and shared volumes folder
    # the only two folders pds really needs write access to
    chown --recursive pds:pds "$WORKSPACE" "$SHARED_VOLUMES"

# Update image and install dependencies
RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade  && \
    apt-get --assume-yes install sed wget openjdk-11-jre-headless pip && \
    apt-get --assume-yes clean

# Copy scripts
COPY scripts "$SCRIPT_FOLDER"
RUN chmod --recursive +x "$SCRIPT_FOLDER"

# Mock folder
COPY mocks "$MOCK_FOLDER"

# Install Flawfinder, Bandit, njsscan and mobsfscan
COPY packages.txt $TOOL_FOLDER/packages.txt
RUN pip install -r $TOOL_FOLDER/packages.txt

# Install the SecHub Product Delegation Server (PDS)
RUN cd "$PDS_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar.sha256sum" && \
    # download pds
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check sechub-pds-$PDS_VERSION.jar.sha256sum

# Copy PDS configfile
COPY pds-config.json "/$PDS_FOLDER/pds-config.json"

# Copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# Create the PDS workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER pds

CMD ["/run.sh"]
