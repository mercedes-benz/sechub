# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

# Arguments
ARG PDS_FOLDER="/pds"
ARG SCRIPT_FOLDER="/scripts"
ARG WORKSPACE="/workspace"
ARG FSB_VERSION="1.12.0"
ARG FSB_SHA256SUM="a50bd4741a68c6886bbc03d20da9ded44bce4dd7d0d2eee19ceb338dd644cd55"
ARG PDS_VERSION

# Environment
ENV TOOL_FOLDER="/tools"
ENV DOWNLOAD_FOLDER="/downloads"
ENV MOCK_FOLDER="$SCRIPT_FOLDER/mocks"
ENV PDS_VERSION="${PDS_VERSION}"
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="$SHARED_VOLUMES/uploads"

# Create tool, pds, shared volume and download folder
RUN  mkdir --parents "$TOOL_FOLDER" "$DOWNLOAD_FOLDER" "$PDS_FOLDER" "$SHARED_VOLUME_UPLOAD_DIR" "$WORKSPACE"

# non-root user
# using fixed group and user ids
RUN groupadd --gid 2323 pds \
     && useradd --uid 2323 --no-log-init --create-home --gid pds pds

# Update image and install dependencies
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
    apt-get --assume-yes upgrade  && \
    apt-get --assume-yes install dos2unix unzip wget openjdk-17-jre-headless libxml2-utils tree && \
    apt-get --assume-yes clean

# Install FindSecurityBugs
RUN cd "$DOWNLOAD_FOLDER" && \
    # download pds
    wget --no-verbose "https://github.com/find-sec-bugs/find-sec-bugs/releases/download/version-$FSB_VERSION/findsecbugs-cli-$FSB_VERSION.zip"  && \
    # create sha256sum
    echo "$FSB_SHA256SUM  findsecbugs-cli-$FSB_VERSION.zip" > "findsecbugs-cli-$FSB_VERSION.zip.sha256sum" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check "findsecbugs-cli-$FSB_VERSION.zip.sha256sum" && \
    # extract FindSecurityBugs
    unzip -q "findsecbugs-cli-$FSB_VERSION.zip" -d "$TOOL_FOLDER" && \
    # Convert Windows format to Unix
    dos2unix "$TOOL_FOLDER/findsecbugs.sh" && \
    # make FindSecurityBugs executable
    chmod +x "$TOOL_FOLDER/findsecbugs.sh"

# Install the SecHub Product Delegation Server (PDS)
RUN cd "$PDS_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar.sha256sum" && \
    # download pds
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check sechub-pds-$PDS_VERSION.jar.sha256sum

# Copy PDS configfile
COPY pds-config.json "/$PDS_FOLDER/pds-config.json"

# Copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# Copy findsecuritybugs script into container
COPY findsecbugs_sechub.sh $TOOL_FOLDER/findsecbugs_sechub.sh
RUN chmod +x $TOOL_FOLDER/findsecbugs_sechub.sh

# Copy scripts
COPY scripts $SCRIPT_FOLDER
RUN chmod -R +x $SCRIPT_FOLDER

# Mock folder
COPY mocks $SCRIPT_FOLDER/mocks/

# Create the PDS workspace
WORKDIR "$WORKSPACE"

# Change owner of tool, workspace and pds folder as well as /run.sh
RUN chown --recursive pds:pds $TOOL_FOLDER $SCRIPT_FOLDER $DOWNLOAD_FOLDER $WORKSPACE $PDS_FOLDER $SHARED_VOLUMES /run.sh

# Switch from root to non-root user
USER pds

CMD ["/run.sh"]