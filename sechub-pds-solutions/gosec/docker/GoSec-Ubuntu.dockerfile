# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

LABEL maintainer="SecHub FOSS Team"

# Build args
ARG GO="go1.18.linux-amd64.tar.gz"
ARG GOSEC_VERSION="2.11.0"
ARG PDS_FOLDER="/pds"
ARG PDS_VERSION="0.26.2"
ARG SCRIPT_FOLDER="/scripts"
ARG WORKSPACE="/workspace"

# Environment variables in container
ENV DOWNLOAD_FOLDER="/downloads"
ENV MOCK_FOLDER="$SCRIPT_FOLDER/mocks"
ENV PDS_VERSION="${PDS_VERSION}"
ENV SHARED_VOLUMES="/shared_volumes"
ENV SHARED_VOLUME_UPLOAD_DIR="$SHARED_VOLUMES/uploads"
ENV TOOL_FOLDER="/tools"
ENV USER="gosec"
ENV UID="2323"
ENV GID="${UID}"

# non-root user
# using fixed group and user ids
# gosec needs a home directory for the cache
RUN groupadd --gid "$GID" "$USER" && \
    useradd --uid "$UID" --gid "$GID" --no-log-init --create-home "$USER"

# Create folders & change owner of folders
RUN mkdir --parents "$PDS_FOLDER" "$SCRIPT_FOLDER" "$TOOL_FOLDER" "$WORKSPACE" "$DOWNLOAD_FOLDER" "MOCK_FOLDER" "$SHARED_VOLUME_UPLOAD_DIR" && \
    # Change owner and workspace and shared volumes folder
    # the only two folders pds really needs write access to
    chown --recursive "$USER:$USER" "$WORKSPACE" "$SHARED_VOLUMES"

# Copy mock file
COPY mock.sarif.json "$MOCK_FOLDER"/mock.sarif.json
# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER"/pds-config.json
# Copy GoSec scripts
COPY gosec.sh "$SCRIPT_FOLDER"/gosec.sh
COPY gosec_mock.sh "$SCRIPT_FOLDER"/gosec_mock.sh
# Copy run script into container
COPY run.sh /run.sh

# Set execute permissions for scripts
RUN chmod +x /run.sh "$SCRIPT_FOLDER"/gosec.sh "$SCRIPT_FOLDER"/gosec_mock.sh

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get -qq update && \
    apt-get -qq --assume-yes upgrade && \
    apt-get -qq --assume-yes install w3m wget openjdk-11-jre-headless && \
    apt-get -qq --assume-yes clean

# Install Go
RUN cd "$DOWNLOAD_FOLDER" && \
    # Get checksum from Go download site
    GO_CHECKSUM=`w3m https://go.dev/dl/ | grep "$GO" | tail -1 | awk '{print $6}'` && \
    # create checksum file
    echo "$GO_CHECKSUM $GO" > "$GO.sha256sum" && \
    # download Go
    wget --no-verbose https://go.dev/dl/"${GO}" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check "$GO.sha256sum" && \
    # extract Go
    tar --extract --file "$GO" --directory /usr/local/ && \
    # remove go tar.gz
    rm "$GO" && \
    # add Go to path
    echo 'export PATH="/usr/local/go/bin:$PATH":' >> /root/.bashrc

# Install GoSec
RUN cd "$DOWNLOAD_FOLDER" && \
    # download gosec
    wget --no-verbose https://github.com/securego/gosec/releases/download/v${GOSEC_VERSION}/gosec_${GOSEC_VERSION}_linux_amd64.tar.gz && \
    # download checksum
    wget --no-verbose https://github.com/securego/gosec/releases/download/v${GOSEC_VERSION}/gosec_${GOSEC_VERSION}_checksums.txt && \
    # verify checksum
    sha256sum --check --ignore-missing "gosec_${GOSEC_VERSION}_checksums.txt" && \
    # create gosec folder
    mkdir --parents "$TOOL_FOLDER/gosec" && \
    # unpack gosec
    tar --extract --ungzip --file "gosec_${GOSEC_VERSION}_linux_amd64.tar.gz" --directory "$TOOL_FOLDER/gosec" && \
    # Remove gosec tar.gz
    rm "gosec_${GOSEC_VERSION}_linux_amd64.tar.gz"

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
