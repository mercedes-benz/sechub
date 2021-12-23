# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

# Folders
ARG PDS_FOLDER="/pds"
ARG SCRIPT_FOLDER="/scripts"
ENV TOOL_FOLDER="/tools"
ARG WORKSPACE="/workspace"
ENV DOWNLOAD_FOLDER="/downloads"
ENV MOCK_FOLDER="$SCRIPT_FOLDER/mocks"

# PDS
ENV PDS_VERSION=0.24.0

# Go
ARG GO="go1.17.5.linux-amd64.tar.gz"
ARG GO_CHECKSUM="bd78114b0d441b029c8fe0341f4910370925a4d270a6a590668840675b0c653e"

# GoSec
ARG GOSEC_VERSION="2.9.5"

# Shared volumes
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="$SHARED_VOLUMES/uploads"

# non-root user
# using fixed group and user ids
# gosec needs a home directory for the cache
RUN groupadd --gid 2323 gosec \
     && useradd --uid 2323 --no-log-init --create-home --gid gosec gosec

# Create folders & change owner of folders
RUN mkdir --parents "$PDS_FOLDER" "$SCRIPT_FOLDER" "$TOOL_FOLDER" "$WORKSPACE" "$DOWNLOAD_FOLDER" "MOCK_FOLDER" "$SHARED_VOLUME_UPLOAD_DIR" && \
    chown --recursive gosec:gosec "$DOWNLOAD_FOLDER" "$TOOL_FOLDER" "$WORKSPACE" "$SCRIPT_FOLDER" "$PDS_FOLDER" "$SHARED_VOLUMES"

# Update image and install dependencies
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
    apt-get upgrade --assume-yes && \
    apt-get install --assume-yes wget openjdk-11-jre-headless && \
    apt-get clean

# Install Go
RUN cd "$DOWNLOAD_FOLDER" && \
    # create checksum file
    echo "$GO_CHECKSUM $GO" > $GO.sha256sum && \
    # download Go
    wget --no-verbose https://dl.google.com/go/${GO} && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check $GO.sha256sum && \
    # extract Go
    tar --extract --file $GO --directory "/usr/local/" && \
    # remove go tar.gz
    rm $GO && \
    # add Go to path
    echo 'export PATH="/usr/local/go/bin:$PATH":' >> /root/.bashrc

# Switch from root to non-root user
USER gosec

# Copy mock file
COPY mock.sarif.json "$MOCK_FOLDER"/mock.sarif.json

# Copy PDS configfile
COPY pds-config.json /$PDS_FOLDER/pds-config.json

# Install GoSec
RUN cd "$DOWNLOAD_FOLDER" && \
    # download gosec
    wget --no-verbose https://github.com/securego/gosec/releases/download/v${GOSEC_VERSION}/gosec_${GOSEC_VERSION}_linux_amd64.tar.gz && \
    # download checksum
    wget --no-verbose https://github.com/securego/gosec/releases/download/v${GOSEC_VERSION}/gosec_${GOSEC_VERSION}_checksums.txt && \
    # verify checksum
    sha256sum --check --ignore-missing "gosec_${GOSEC_VERSION}_checksums.txt" && \
    # create gosec folder
    mkdir --parents "$TOOL_FOLDER/gosec" && \
    # unpack gosec
    tar --extract --ungzip --file "gosec_${GOSEC_VERSION}_linux_amd64.tar.gz" --directory "$TOOL_FOLDER/gosec" && \
    # Remove gosec tar.gz
    rm "gosec_${GOSEC_VERSION}_linux_amd64.tar.gz"

# Install the Product Delegation Server (PDS)
RUN cd /pds && \
    # download checksum file
    wget --no-verbose "https://github.com/Daimler/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar.sha256sum" && \
    # download pds
    wget --no-verbose "https://github.com/Daimler/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check sechub-pds-$PDS_VERSION.jar.sha256sum

# Copy run script into container
COPY run.sh /run.sh

# Copy scripts
COPY gosec.sh $SCRIPT_FOLDER/gosec.sh
COPY gosec_mock.sh $SCRIPT_FOLDER/gosec_mock.sh

# Switch back to root
USER root

# Change owner of run.sh
RUN chown gosec:gosec /run.sh 

# Set execute permissions for scripts
RUN chmod +x /run.sh $SCRIPT_FOLDER/gosec.sh $SCRIPT_FOLDER/gosec_mock.sh

# Switch from root to non-root user
USER gosec

# Set workspace
WORKDIR "$WORKSPACE"

CMD ["/run.sh"]
