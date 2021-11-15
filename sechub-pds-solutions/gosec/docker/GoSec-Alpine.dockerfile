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

# GoSec
ARG GOSEC_VERSION="2.9.1"
ARG GOSEC_CHECKSUM="34505685c89f702719177e0c8a2b43907026d8c79b70fbaa76153fbd53603b66"

# Shared volumes
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="$SHARED_VOLUMES/uploads"

# non-root user
# using fixed group and user ids
# gosec needs a home directory for the cache
RUN addgroup --gid 2323 gosec \
     && adduser --uid 2323 --disabled-password --ingroup gosec gosec

RUN apk update --no-cache && \
    apk add --no-cache go openjdk11-jre-headless wget unzip tar

# Create script folder
COPY gosec.sh $SCRIPT_FOLDER/gosec.sh
RUN chmod +x $SCRIPT_FOLDER/gosec.sh

COPY gosec_mock.sh $SCRIPT_FOLDER/gosec_mock.sh
RUN chmod +x $SCRIPT_FOLDER/gosec_mock.sh

# Setup mock product
RUN mkdir "$MOCK_FOLDER"
COPY mock.sarif.json "$MOCK_FOLDER"/mock.sarif.json 

# Create download folder
RUN mkdir "$DOWNLOAD_FOLDER"

# Install GoSec
RUN cd "$DOWNLOAD_FOLDER" && \
    # create checksum file
    echo "$GOSEC_CHECKSUM  gosec_${GOSEC_VERSION}_linux_amd64.tar.gz" > gosec_${GOSEC_VERSION}_linux_amd64.tar.gz.sha256sum && \
    # download gosec
    wget https://github.com/securego/gosec/releases/download/v${GOSEC_VERSION}/gosec_${GOSEC_VERSION}_linux_amd64.tar.gz && \
    # verify checksum
    sha256sum -c "gosec_${GOSEC_VERSION}_linux_amd64.tar.gz.sha256sum" && \
    # create gosec folder
    mkdir -p "$TOOL_FOLDER/gosec" && \
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
    sha256sum -c sechub-pds-$PDS_VERSION.jar.sha256sum

# Copy PDS configfile
COPY pds-config.json /$PDS_FOLDER/pds-config.json

# Copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# Create shared volumes and upload dir
RUN mkdir --parents "$SHARED_VOLUME_UPLOAD_DIR"

# Create the PDS workspace
WORKDIR "$WORKSPACE"

# Change owner of tool, workspace and pds folder as well as /run.sh
RUN chown --recursive gosec:gosec $DOWNLOAD_FOLDER $TOOL_FOLDER $SCRIPT_FOLDER $WORKSPACE $PDS_FOLDER $SHARED_VOLUMES /run.sh

# switch from root to non-root user
USER gosec

CMD ["/run.sh"]
