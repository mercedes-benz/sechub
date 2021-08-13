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

# PDS
ENV PDS_VERSION=0.23.0
ARG PDS_CHECKSUM="d581332f25b03c3961f499af3ad7d1cf55a5f609fb88b50651b7c1737f4ca16b"

# GoSec
ARG GOSEC_VERSION="2.8.1"
ARG GOSEC_CHECKSUM="b9632585292c5ebc749b0afe064661bee7ea422fc7c54a5282a001e52c8ed30d"

# Shared volumes
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="$SHARED_VOLUMES/uploads"

# non-root user
# using fixed group and user ids
# gosec needs a home directory for the cache
RUN addgroup --gid 2323 gosec \
     && adduser --uid 2323 --disabled-password --ingroup gosec gosec

RUN apk update && \
    apk add go openjdk11-jre-headless wget unzip tar

# Create script folder
COPY gosec.sh $SCRIPT_FOLDER/gosec.sh
RUN chmod +x $SCRIPT_FOLDER/gosec.sh

# Install GoSec
RUN cd /tmp && \
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
RUN chown --recursive gosec:gosec $TOOL_FOLDER $SCRIPT_FOLDER $WORKSPACE $PDS_FOLDER $SHARED_VOLUMES /run.sh

# switch from root to non-root user
USER gosec

CMD ["/run.sh"]
