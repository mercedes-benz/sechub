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

# PDS
ENV PDS_VERSION=0.24.0
ARG PDS_CHECKSUM="ecc69561109ee98a57a087fd9e6a4980a38ac72d07467d6c69579c83c16b3255"

# Shared volumes
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="$SHARED_VOLUMES/uploads"

# non-root user
# using fixed group and user ids
RUN groupadd --gid 2323 pds \
     && useradd --uid 2323 --no-log-init --create-home --gid pds pds

# Update image and install dependencies
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
    apt-get upgrade --assume-yes && \
    apt-get install --assume-yes wget openjdk-11-jre-headless pip && \
    apt-get clean

# Copy scripts
COPY flawfinder.sh $SCRIPT_FOLDER/flawfinder.sh
RUN chmod +x $SCRIPT_FOLDER/flawfinder.sh

COPY bandit.sh $SCRIPT_FOLDER/bandit.sh
RUN chmod +x $SCRIPT_FOLDER/bandit.sh

COPY njsscan.sh $SCRIPT_FOLDER/njsscan.sh
RUN chmod +x $SCRIPT_FOLDER/njsscan.sh

COPY mobsfscan.sh $SCRIPT_FOLDER/mobsfscan.sh
RUN chmod +x $SCRIPT_FOLDER/mobsfscan.sh

# Create tool folder
RUN  mkdir --parents "$TOOL_FOLDER"

# Create download folder
RUN  mkdir --parents "$DOWNLOAD_FOLDER"

# Install Flawfinder, Bandit, njsscan and mobsfscan
RUN pip install flawfinder bandit bandit_sarif_formatter njsscan mobsfscan

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
RUN chown --recursive pds:pds $TOOL_FOLDER $SCRIPT_FOLDER $DOWNLOAD_FOLDER $WORKSPACE $PDS_FOLDER $SHARED_VOLUMES /run.sh

# Switch from root to non-root user
#USER pds

CMD ["/run.sh"]
