# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

# User
ENV USER="gosec"

# Folders
ARG PDS_FOLDER="/pds"
ARG SCRIPT_FOLDER="/scripts"
ENV TOOL_FOLDER="/tools"
ARG WORKSPACE="/workspace"
ENV DOWNLOAD_FOLDER="/downloads"
ENV MOCK_FOLDER="$SCRIPT_FOLDER/mocks"

# PDS
ENV PDS_VERSION=0.24.0

# GoSec
ARG GOSEC_VERSION="2.9.5"
ARG GOSEC_CHECKSUM="524330ccda004a9af0ef1b78b712df02144a307a15b57a6528f12762f73c8d8e"

# Shared volumes
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="$SHARED_VOLUMES/uploads"

# non-root user
# using fixed group and user ids
# gosec needs a home directory for the cache
RUN addgroup --gid 2323 "$USER" \
     && adduser --uid 2323 --disabled-password --ingroup "$USER" "$USER"

# Create folders & change owner of folders
RUN mkdir --parents "$PDS_FOLDER" "$SCRIPT_FOLDER" "$TOOL_FOLDER" "$WORKSPACE" "$DOWNLOAD_FOLDER" "MOCK_FOLDER" "$SHARED_VOLUME_UPLOAD_DIR" && \
    chown --recursive "$USER:$USER" "$DOWNLOAD_FOLDER" "$TOOL_FOLDER" "$WORKSPACE" "$SCRIPT_FOLDER" "$PDS_FOLDER" "$SHARED_VOLUMES"

RUN apk update --no-cache && \
    apk add --no-cache go openjdk11-jre-headless wget unzip tar

# Switch from root to non-root user
USER "$USER"

# Copy mock file
COPY mock.sarif.json "$MOCK_FOLDER"/mock.sarif.json

# Copy PDS configfile
COPY pds-config.json /$PDS_FOLDER/pds-config.json

# Install GoSec
RUN cd "$DOWNLOAD_FOLDER" && \
    # create checksum file
    echo "$GOSEC_CHECKSUM  gosec_${GOSEC_VERSION}_linux_amd64.tar.gz" > gosec_${GOSEC_VERSION}_linux_amd64.tar.gz.sha256sum && \
    # download gosec
    wget --no-verbose https://github.com/securego/gosec/releases/download/v${GOSEC_VERSION}/gosec_${GOSEC_VERSION}_linux_amd64.tar.gz && \
    # verify checksum
    sha256sum -c "gosec_${GOSEC_VERSION}_linux_amd64.tar.gz.sha256sum" && \
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
    sha256sum -c sechub-pds-$PDS_VERSION.jar.sha256sum

# Copy run script into container
COPY run.sh /run.sh

# Copy scripts
COPY gosec.sh $SCRIPT_FOLDER/gosec.sh
COPY gosec_mock.sh $SCRIPT_FOLDER/gosec_mock.sh

# Switch back to root
USER root

# Set execute permissions for scripts
RUN chmod +x /run.sh $SCRIPT_FOLDER/gosec.sh $SCRIPT_FOLDER/gosec_mock.sh

# Switch from root to non-root user
USER "$USER"

# Set workspace
WORKDIR "$WORKSPACE"

CMD ["/run.sh"]
