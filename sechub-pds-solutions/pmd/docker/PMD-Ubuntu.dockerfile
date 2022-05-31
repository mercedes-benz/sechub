# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

LABEL maintainer="SecHub FOSS Team"

# Build args
ARG PMD_VERSION="6.46.0"
ARG PDS_FOLDER="/pds"
ARG PDS_VERSION="0.27.0"
ARG SCRIPT_FOLDER="/scripts"
ARG WORKSPACE="/workspace"

# Environment variables in container
ENV DOWNLOAD_FOLDER="/downloads"
ENV MOCK_FOLDER="$SCRIPT_FOLDER/mocks"
ENV PDS_VERSION="${PDS_VERSION}"
ENV SCRIPT_FOLDER="${SCRIPT_FOLDER}"
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="$SHARED_VOLUMES/uploads"
ENV TOOL_FOLDER="/tools"
ENV USER="pmd"
ENV UID="2323"
ENV GID="${UID}"

# non-root user
# using fixed group and user ids
RUN groupadd --gid "$GID" "$USER" && \
    useradd --uid "$UID" --gid "$GID" --no-log-init --create-home "$USER"

# Create folders & change owner of folders
RUN mkdir --parents "$PDS_FOLDER" "$SCRIPT_FOLDER" "$TOOL_FOLDER" "$WORKSPACE" "$DOWNLOAD_FOLDER" "$MOCK_FOLDER" "$SHARED_VOLUME_UPLOAD_DIR" && \
    # Change owner and workspace and shared volumes folder
    # the only two folders pds really needs write access to
    chown --recursive "$USER:$USER" "$WORKSPACE" "$SHARED_VOLUMES"

# Copy mock folder
COPY mocks "$MOCK_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER"/pds-config.json

# Copy PMD scripts and rulesets
COPY pmd.sh "$SCRIPT_FOLDER"/pmd.sh
COPY ruleset-security.xml "$SCRIPT_FOLDER"/ruleset-security.xml
COPY ruleset-all.xml "$SCRIPT_FOLDER"/ruleset-all.xml

# Copy run script into container
COPY run.sh /run.sh

# Set execute permissions for scripts
RUN chmod +x /run.sh "$SCRIPT_FOLDER"/pmd.sh

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade && \
    apt-get --assume-yes install w3m wget openjdk-11-jre-headless unzip && \
    apt-get --assume-yes clean

# Install PMD
RUN cd "$DOWNLOAD_FOLDER" && \
    # download pmd
    wget --no-verbose https://github.com/pmd/pmd/releases/download/pmd_releases%2F${PMD_VERSION}/pmd-bin-${PMD_VERSION}.zip && \
    # create pmd folder
    mkdir --parents "$TOOL_FOLDER/pmd/bin" "$TOOL_FOLDER/pmd/lib" && \
    # unpack pmd
    unzip -j pmd-bin-${PMD_VERSION}.zip pmd-bin-${PMD_VERSION}/bin/* -d "$TOOL_FOLDER/pmd/bin" && \
    unzip -j pmd-bin-${PMD_VERSION}.zip pmd-bin-${PMD_VERSION}/lib/* -d "$TOOL_FOLDER/pmd/lib" && \
    # Remove pmd zip
    rm pmd-bin-${PMD_VERSION}.zip

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
USER "$USER"

CMD ["/run.sh"]
