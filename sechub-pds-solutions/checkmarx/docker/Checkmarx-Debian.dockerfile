# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Checkmarx+PDS Image"
LABEL org.opencontainers.image.description="A container which combines a Checkmarx Wrapper script with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

USER root

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get upgrade --assume-yes && \
    apt-get install --assume-yes wget && \
    apt-get clean

# Install SecHub OWASP ZAP wrapper
RUN cd "$TOOL_FOLDER" && \
    # download checksum file
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-wrapper-checkmarx-$PDS_VERSION.jar.sha256sum" && \
    # download wrapper jar
    wget --no-verbose "https://github.com/mercedes-benz/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-wrapper-checkmarx-$PDS_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check sechub-pds-wrapper-checkmarx-$PDS_VERSION.jar.sha256sum && \
    ln --symbolic sechub-pds-wrapper-checkmarx-$PDS_VERSION.jar wrapper-checkmarx.jar

# Copy mock folders
COPY mocks/ "$MOCK_FOLDER"

# Setup scripts
COPY checkmarx.sh ${SCRIPT_FOLDER}/checkmarx.sh
COPY checkmarx-mock.sh ${SCRIPT_FOLDER}/checkmarx-mock.sh

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER/pds-config.json"

# Copy run script into container
COPY run.sh /run.sh

# Make scripts executable
RUN chmod +x ${SCRIPT_FOLDER}/checkmarx.sh ${SCRIPT_FOLDER}/checkmarx-mock.sh /run.sh

# Create the PDS workspace
WORKDIR "$WORKSPACE"

# Change owner of tool, workspace and pds folder as well as /run.sh
RUN chown --recursive "$USER:$USER" $TOOL_FOLDER ${SCRIPT_FOLDER} $WORKSPACE $PDS_FOLDER ${SHARED_VOLUMES} /run.sh

# Switch from root to non-root user
USER "$USER"

CMD ["/run.sh"]
