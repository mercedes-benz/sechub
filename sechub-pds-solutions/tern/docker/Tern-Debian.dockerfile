# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Tern+PDS Image"
LABEL org.opencontainers.image.description="A container which combines Tern with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

ARG SPDX_TOOL_VERISON="1.0.4"
ARG SPDX_TOOL_CHECKSUM="e8da16d744d9a39dbc0420f776e0ebae71ce6a0be722941ada2e0e0f755cc4d0  tools-java-1.0.4-jar-with-dependencies.jar"

ENV SPDX_TOOL_VERISON="${SPDX_TOOL_VERISON}"

# execute commands as root
USER root

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get --quiet update && \
    apt-get --quiet --assume-yes upgrade && \
    apt-get --quiet --assume-yes install wget \
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

# Install SPDX Tools Java converter
RUN cd "$TOOL_FOLDER" && \
    # download SPDX Tools Java 
    wget --no-verbose "https://repo1.maven.org/maven2/org/spdx/tools-java/${SPDX_TOOL_VERISON}/tools-java-${SPDX_TOOL_VERISON}-jar-with-dependencies.jar" && \
    # create checksum file
    echo "$SPDX_TOOL_CHECKSUM" > checksum-spdx-tool.sha256sum && \
    # check against checksum file
    sha256sum -c checksum-spdx-tool.sha256sum

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER"/pds-config.json

# Copy scripts
COPY scripts "$SCRIPT_FOLDER"
RUN chmod --recursive +x "$SCRIPT_FOLDER"

# Copy mock folder
COPY mocks "$MOCK_FOLDER"