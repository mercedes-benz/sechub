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
ENV PDS_VERSION="0.26.0"

# Shared volumes
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="$SHARED_VOLUMES/uploads"

# Python version
ARG PYTHON_VERSION="3.9"

# Scancode
ENV SCANCODE_VERSION="30.1.0"
ARG SCANCODE_CHECKSUM="a9e43fdef934335e69e4abf77896225545d3e1fdbbd477ebabc37a4fa5ee2015  scancode-toolkit-30.1.0_py39-linux.tar.xz"

# SPDX Tool
ENV SPDX_TOOL_VERISON="1.0.4"
ARG SPDX_TOOL_CHECKSUM="e8da16d744d9a39dbc0420f776e0ebae71ce6a0be722941ada2e0e0f755cc4d0  tools-java-1.0.4-jar-with-dependencies.jar"

# non-root user
# using fixed group and user ids
RUN groupadd --gid 2323 pds \
     && useradd --uid 2323 --no-log-init --create-home --gid pds pds

# Create tool, pds, shared volume and download folder
RUN  mkdir --parents "$TOOL_FOLDER" "$DOWNLOAD_FOLDER" "$PDS_FOLDER" "$SHARED_VOLUME_UPLOAD_DIR" "$WORKSPACE" && \
    # Change owner and workspace and shared volumes folder
    # the only two folders pds really needs write access to
    chown --recursive pds:pds "$WORKSPACE" "$SHARED_VOLUMES"

# Update image and install dependencies
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
    apt-get --assume-yes upgrade  && \
    apt-get --assume-yes install wget \
                                 tar \
                                 openjdk-11-jre-headless \
                                 "python${PYTHON_VERSION}" \
                                 "python${PYTHON_VERSION}-distutils" \
                                 python-dev \
                                 bzip2 \
                                 xz-utils \
                                 zlib1g \
                                 libxml2-dev \
                                 libxslt1-dev \
                                 libgomp1 \
                                 libpopt0 && \
    apt-get --assume-yes clean

# Install ScanCode
RUN cd "$DOWNLOAD_FOLDER" && \
    # create checksum file
    echo "$SCANCODE_CHECKSUM" > checksum-scancode-toolkit.sha256sum && \
    # download ScanCode Toolkit
    wget --no-verbose "https://github.com/nexB/scancode-toolkit/releases/download/v${SCANCODE_VERSION}/scancode-toolkit-${SCANCODE_VERSION}_py39-linux.tar.xz" && \
    # check against checksum file
    sha256sum -c checksum-scancode-toolkit.sha256sum && \
    # extract ScanCode
    tar --extract --file "scancode-toolkit-${SCANCODE_VERSION}_py39-linux.tar.xz" --directory "$TOOL_FOLDER/" && \
    # delete downloded `.tar.xz`
    rm "scancode-toolkit-${SCANCODE_VERSION}_py39-linux.tar.xz" && \
    # make tool folder writable
    chown --recursive pds:pds "$TOOL_FOLDER/"

# Install SPDX Tools Java converter
RUN cd "$TOOL_FOLDER" && \
    # download SPDX Tools Java 
    wget --no-verbose "https://repo1.maven.org/maven2/org/spdx/tools-java/${SPDX_TOOL_VERISON}/tools-java-${SPDX_TOOL_VERISON}-jar-with-dependencies.jar" && \
    # create checksum file
    echo "$SPDX_TOOL_CHECKSUM" > checksum-spdx-tool.sha256sum && \
    # check against checksum file
    sha256sum -c checksum-spdx-tool.sha256sum

# Install the SecHub Product Delegation Server (PDS)
RUN cd "$PDS_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar.sha256sum" && \
    # download pds
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check sechub-pds-$PDS_VERSION.jar.sha256sum

# Copy scripts
COPY scripts $SCRIPT_FOLDER
RUN chmod -R +x $SCRIPT_FOLDER

# Mock folder
COPY mocks $SCRIPT_FOLDER/mocks/

# Copy PDS configfile
COPY pds-config.json "/$PDS_FOLDER/pds-config.json"

# Copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# Create the PDS workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER pds

# Configure ScanCode
RUN cd "$TOOL_FOLDER/scancode-toolkit-$SCANCODE_VERSION/" && \
    ./configure

CMD ["/run.sh"]