# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub cloc+PDS Image"
LABEL org.opencontainers.image.description="A container which combines cloc with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

# Build args
ARG CLOC_VERSION="1.94"
ENV CLOC_TAR="cloc-$CLOC_VERSION.tar.gz"

USER root

# Copy mock folder
COPY mocks "$MOCK_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER"/pds-config.json

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade && \
    apt-get --assume-yes install wget perl && \
    apt-get --assume-yes clean

# Install cloc
RUN cd "$DOWNLOAD_FOLDER" && \
    # download cloc
    wget --no-verbose https://github.com/AlDanial/cloc/releases/download/v"$CLOC_VERSION"/"$CLOC_TAR" && \
    # extract cloc
    tar --extract --file "$CLOC_TAR" cloc-${CLOC_VERSION}/cloc && \
    # copy cloc binary to /usr/local/bin
    mv cloc-"$CLOC_VERSION"/cloc /usr/local/bin/ && \
    # remove cloc tar
    rm --recursive --force $CLOC_TAR cloc-${CLOC_VERSION} &&  \

# Copy scripts
COPY scripts $SCRIPT_FOLDER
RUN chmod --recursive +x $SCRIPT_FOLDER

# Set workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER "$USER"
