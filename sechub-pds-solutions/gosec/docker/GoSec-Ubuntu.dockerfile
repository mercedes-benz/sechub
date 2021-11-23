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
ARG PDS_CHECKSUM="ecc69561109ee98a57a087fd9e6a4980a38ac72d07467d6c69579c83c16b3255"

# Go
ARG GO="go1.17.3.linux-amd64.tar.gz"
ARG GO_CHECKSUM="550f9845451c0c94be679faf116291e7807a8d78b43149f9506c1b15eb89008c"

# GoSec
ARG GOSEC_VERSION="2.9.2"

# Shared volumes
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="$SHARED_VOLUMES/uploads"

# non-root user
# using fixed group and user ids
# gosec needs a home directory for the cache
RUN groupadd --gid 2323 gosec \
     && useradd --uid 2323 --no-log-init --create-home --gid gosec gosec

# Update image and install dependencies
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
    apt-get upgrade --assume-yes && \
    apt-get install --assume-yes wget openjdk-11-jre-headless && \
    apt-get clean

# Create script folder
COPY gosec.sh $SCRIPT_FOLDER/gosec.sh
RUN chmod +x $SCRIPT_FOLDER/gosec.sh

COPY gosec_mock.sh $SCRIPT_FOLDER/gosec_mock.sh
RUN chmod +x $SCRIPT_FOLDER/gosec_mock.sh

# Setup mock folder
RUN mkdir "$MOCK_FOLDER"
COPY mock.sarif.json "$MOCK_FOLDER"/mock.sarif.json

# Create download folder
RUN mkdir "$DOWNLOAD_FOLDER"

# Install Go
RUN cd "$DOWNLOAD_FOLDER" && \
    # create checksum file
    echo "$GO_CHECKSUM $GO" > $GO.sha256sum && \
    # download Go
    wget https://dl.google.com/go/${GO} && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check $GO.sha256sum && \
    # extract Go
    tar --extract --file $GO --directory "/usr/local/" && \
    # remove go tar.gz
    rm $GO && \
    # add Go to path
    echo 'export PATH="/usr/local/go/bin:$PATH":' >> /root/.bashrc

# Install GoSec
RUN cd "$DOWNLOAD_FOLDER" && \
    # download gosec
    wget https://github.com/securego/gosec/releases/download/v${GOSEC_VERSION}/gosec_${GOSEC_VERSION}_linux_amd64.tar.gz && \
    # download checksum
    wget https://github.com/securego/gosec/releases/download/v${GOSEC_VERSION}/gosec_${GOSEC_VERSION}_checksums.txt && \
    # verify checksum
    sha256sum --check --ignore-missing "gosec_${GOSEC_VERSION}_checksums.txt" && \
    # create gosec folder
    mkdir --parents "$TOOL_FOLDER/gosec" && \
    # unpack gosec
    tar --extract --ungzip --file "gosec_${GOSEC_VERSION}_linux_amd64.tar.gz" --directory "$TOOL_FOLDER/gosec" && \
    # Remove gosec tar.gz
    rm "gosec_${GOSEC_VERSION}_linux_amd64.tar.gz"

# Install the Product Delegation Server (PDS)
RUN mkdir --parents "$PDS_FOLDER" && \
    cd /pds && \
    # create checksum file
    echo "$PDS_CHECKSUM  sechub-pds-$PDS_VERSION.jar" > sechub-pds-$PDS_VERSION.jar.sha256sum && \
    # download pds
    wget "https://github.com/Daimler/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check sechub-pds-$PDS_VERSION.jar.sha256sum

# Create shared volumes and upload dir
RUN mkdir --parents "$SHARED_VOLUME_UPLOAD_DIR"

# Copy PDS configfile
COPY pds-config.json /$PDS_FOLDER/pds-config.json

# Copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# Create the PDS workspace
WORKDIR "$WORKSPACE"

# Change owner of tool, workspace and pds folder as well as /run.sh
RUN chown --recursive gosec:gosec $DOWNLOAD_FOLDER $TOOL_FOLDER $SCRIPT_FOLDER $WORKSPACE $PDS_FOLDER $SHARED_VOLUMES /run.sh

# Switch from root to non-root user
USER gosec

CMD ["/run.sh"]