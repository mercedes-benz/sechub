# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

LABEL maintainer="SecHub FOSS Team"

# Build args
ARG PDS_FOLDER="/pds"
ARG PDS_VERSION="0.27.0"
ARG SCRIPT_FOLDER="/scripts"
ARG WORKSPACE="/workspace"

# Environment variables in container
ENV DOWNLOAD_FOLDER="/downloads"
ENV MOCK_FOLDER="$SCRIPT_FOLDER/mocks"
ENV PDS_VERSION="${PDS_VERSION}"
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="$SHARED_VOLUMES/uploads"
ENV TOOL_FOLDER="/tools"
ENV USER="tern"
ENV UID="3232"
ENV GID="${UID}"

# non-root user
# using fixed group and user ids
RUN groupadd --gid "$GID" "$USER" && \
    useradd --uid "$UID" --gid "$GID" --no-log-init --create-home "$USER"

# Create folders & change owner of folders
RUN mkdir --parents "$PDS_FOLDER" "$SCRIPT_FOLDER" "$TOOL_FOLDER" "$WORKSPACE" "$DOWNLOAD_FOLDER" "MOCK_FOLDER" "$SHARED_VOLUME_UPLOAD_DIR" && \
    # Change owner and workspace and shared volumes folder
    # the only two folders pds really needs write access to
    chown --recursive "$USER:$USER" "$WORKSPACE" "$SHARED_VOLUMES"

# Copy mock folder
COPY mocks "$MOCK_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER"/pds-config.json

# Copy Tern scripts
COPY tern.sh "$SCRIPT_FOLDER"/tern.sh
COPY tern_mock.sh "$SCRIPT_FOLDER"/tern_mock.sh

# Copy run script into container
COPY run.sh /run.sh

# Set execute permissions for scripts
RUN chmod +x /run.sh "$SCRIPT_FOLDER"/tern.sh "$SCRIPT_FOLDER"/tern_mock.sh

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get --quiet update && \
    apt-get --quiet --assume-yes upgrade && \
    apt-get --quiet --assume-yes install wget openjdk-11-jre-headless attr findutils jq gcc skopeo python3-pip git sudo fuse-overlayfs fuse3 && \
    apt-get --quiet --assume-yes clean

RUN pip3 install --no-warn-script-location tern

# Install the SecHub Product Delegation Server (PDS)
RUN cd "$PDS_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar.sha256sum" && \
    # download pds
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check sechub-pds-$PDS_VERSION.jar.sha256sum

# Set workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
#USER "$USER"

CMD ["/run.sh"]