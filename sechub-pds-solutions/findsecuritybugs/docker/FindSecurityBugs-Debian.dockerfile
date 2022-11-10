# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub FindSecurityBugs+PDS Image"
LABEL org.opencontainers.image.description="A container which combines FindSecurityBugs with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

# Arguments
ARG FINDSECURITYBUGS_VERSION="1.12.0"
ARG FINDSECURITYBUGS_SHA256SUM="a50bd4741a68c6886bbc03d20da9ded44bce4dd7d0d2eee19ceb338dd644cd55"

# Environment variables in container
ENV FINDSECURITYBUGS_VERSION="${FINDSECURITYBUGS_VERSION}"

USER root

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade  && \
    apt-get --assume-yes install dos2unix unzip wget openjdk-17-jre-headless libxml2-utils tree && \
    apt-get --assume-yes clean

# Install FindSecurityBugs
RUN cd "$DOWNLOAD_FOLDER" && \
    # download pds
    wget --no-verbose "https://github.com/find-sec-bugs/find-sec-bugs/releases/download/version-$FINDSECURITYBUGS_VERSION/findsecbugs-cli-$FINDSECURITYBUGS_VERSION.zip"  && \
    # create sha256sum
    echo "$FINDSECURITYBUGS_SHA256SUM  findsecbugs-cli-$FINDSECURITYBUGS_VERSION.zip" > "findsecbugs-cli-$FINDSECURITYBUGS_VERSION.zip.sha256sum" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check "findsecbugs-cli-$FINDSECURITYBUGS_VERSION.zip.sha256sum" && \
    # extract FindSecurityBugs
    unzip -q "findsecbugs-cli-$FINDSECURITYBUGS_VERSION.zip" -d "$TOOL_FOLDER" && \
    # Convert Windows format to Unix
    dos2unix "$TOOL_FOLDER/findsecbugs.sh" && \
    # make FindSecurityBugs executable
    chmod +x "$TOOL_FOLDER/findsecbugs.sh"

# Copy PDS configfile
COPY pds-config.json "/$PDS_FOLDER/pds-config.json"

# Copy findsecuritybugs script into container
COPY findsecbugs_sechub.sh $TOOL_FOLDER/findsecbugs_sechub.sh
RUN chmod +x $TOOL_FOLDER/findsecbugs_sechub.sh

# Copy scripts
COPY scripts $SCRIPT_FOLDER
RUN chmod --recursive +x $SCRIPT_FOLDER

# Mock folder
COPY mocks $SCRIPT_FOLDER/mocks/

# Set workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER "$USER"