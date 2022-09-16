# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

LABEL maintainer="SecHub FOSS Team"

# Build args
ARG PDS_FOLDER="/pds"

# The minimum version needs to be 0.29.0
# otherwise the binary uploads do not work
ARG PDS_VERSION="0.31.0"
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

# Copy run script into container
COPY run.sh /run.sh

# Set execute permissions run script
RUN chmod +x /run.sh

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get --quiet update && \
    apt-get --quiet --assume-yes upgrade && \
    apt-get --quiet --assume-yes install wget \
                                         openjdk-11-jre-headless \
                                         attr \
                                         jq \
                                         skopeo \
                                         python3-pip \
                                         git \
                                         bzip2 \
                                         xz-utils \
                                         zlib1g \ 
                                         libxml2-dev \
                                         libxslt1-dev \
                                         libgomp1 \
                                         libpopt0 && \
    apt-get --quiet --assume-yes clean

# Install Tern and Scancode-Toolkit
COPY packages.txt $TOOL_FOLDER/packages.txt
RUN pip install --no-warn-script-location -r $TOOL_FOLDER/packages.txt

# Install the SecHub Product Delegation Server (PDS)
RUN cd "$PDS_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar.sha256sum" && \
    # download pds
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check sechub-pds-$PDS_VERSION.jar.sha256sum

# Copy scripts
COPY scripts "$SCRIPT_FOLDER"
RUN chmod --recursive +x "$SCRIPT_FOLDER"

# Set workspace
WORKDIR "$WORKSPACE"

CMD ["/run.sh"]