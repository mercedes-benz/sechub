# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

ARG CLOC_VERSION
ARG SCC_VERSION

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub loc+PDS Image"
LABEL org.opencontainers.image.description="A container which combines lines of code (LoC) counting analytics tools with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

ENV CLOC_TAR="cloc-$CLOC_VERSION.tar.gz"
ENV SCC_VERSION="${SCC_VERSION}"

USER root

# Copy mock folder
COPY mocks "$MOCK_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER"/pds-config.json

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade && \
    apt-get --assume-yes install perl wget yq && \
    apt-get --assume-yes clean

# Install scc
RUN cd "$DOWNLOAD_FOLDER" && \
    wget --no-verbose "https://github.com/boyter/scc/releases/download/v${SCC_VERSION}/scc_${SCC_VERSION}_Linux_x86_64.tar.gz" && \
    wget --no-verbose "https://github.com/boyter/scc/releases/download/v${SCC_VERSION}/checksums.txt" && \
    grep scc_${SCC_VERSION}_Linux_x86_64.tar.gz checksums.txt | sha256sum --check && \
    tar zxf scc_${SCC_VERSION}_Linux_x86_64.tar.gz scc && \
    mv scc /usr/local/bin/ && \
    # Cleanup download folder
    rm --recursive --force "$DOWNLOAD_FOLDER"/*

# Install cloc
RUN cd "$DOWNLOAD_FOLDER" && \
    # download cloc
    wget --no-verbose https://github.com/AlDanial/cloc/releases/download/v"$CLOC_VERSION"/"$CLOC_TAR" && \
    # extract cloc
    tar --extract --file "$CLOC_TAR" cloc-${CLOC_VERSION}/cloc && \
    # copy cloc binary to /usr/local/bin
    mv cloc-"$CLOC_VERSION"/cloc /usr/local/bin/ && \
    # Cleanup download folder
    rm --recursive --force "$DOWNLOAD_FOLDER"/*

# Copy scripts
COPY scripts $SCRIPT_FOLDER
RUN chmod --recursive +x $SCRIPT_FOLDER

# Set workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER "$USER"
