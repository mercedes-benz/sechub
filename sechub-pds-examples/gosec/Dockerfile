# The image argument needs to be placed on top
ARG IMAGE=ubuntu:20.04

FROM $IMAGE

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/
ARG WORKSPACE="/workspace"
ARG PDS_FOLDER="/pds"
ARG TOOL_FOLDER="/tool"
ARG PDS_VERSION=0.21.0

# non-root user
# using fixed group and user ids
# gosec needs a home directory for the cache
RUN groupadd --gid 2323 gosec \
     && useradd --uid 2323 --no-log-init --create-home --gid gosec gosec

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update
RUN apt-get dist-upgrade --assume-yes
RUN apt-get install --assume-yes wget openjdk-11-jre-headless

# Create tool folder
RUN mkdir -p $TOOL_FOLDER
COPY gosec.sh $TOOL_FOLDER/gosec.sh
RUN chmod +x $TOOL_FOLDER/gosec.sh

# Install Go
RUN cd /tmp && \
    # create checksum file
    echo "013a489ebb3e24ef3d915abe5b94c3286c070dfe0818d5bca8108f1d6e8440d2 go1.16.linux-amd64.tar.gz" > go1.16.linux-amd64.tar.gz.sha256sum && \
    # download Go
    wget https://dl.google.com/go/go1.16.linux-amd64.tar.gz && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check go1.16.linux-amd64.tar.gz.sha256sum && \
    # extract Go
    tar --extract --file go1.16.linux-amd64.tar.gz --directory "/usr/local/" && \
    # add Go to path
    echo 'export PATH="/usr/local/go/bin:$PATH":' >> /root/.bashrc

# Install GoSec
RUN cd /tmp && \
    # download gosec
    wget https://github.com/securego/gosec/releases/download/v2.7.0/gosec_2.7.0_linux_amd64.tar.gz && \
    # download checksum
    wget https://github.com/securego/gosec/releases/download/v2.7.0/gosec_2.7.0_checksums.txt && \
    # verify checksum
    sha256sum --check --ignore-missing gosec_2.7.0_checksums.txt && \
    # create gosec folder
    mkdir -p "$TOOL_FOLDER/gosec" && \
    # unpack gosec
    tar --extract --ungzip --file gosec_2.7.0_linux_amd64.tar.gz --directory "$TOOL_FOLDER/gosec"

# Install the Product Delegation Server (PDS)
RUN mkdir -p pds && \
    cd /pds && \
    # create checksum file
    echo "f3101a6a2a96b39edf493249f2300c627fa01fcb7a6b96c5ae1edfbe70a31d46  sechub-pds-$PDS_VERSION.jar" > sechub-pds-$PDS_VERSION.jar.sha256sum && \
    # download pds
    wget "https://github.com/Daimler/sechub/releases/download/v$PDS_VERSION-pds/sechub-pds-$PDS_VERSION.jar" && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check sechub-pds-$PDS_VERSION.jar.sha256sum

# Create the PDS workspace
RUN mkdir -p $WORKSPACE    

# Copy PDS configfile
COPY pds-config.json /$PDS_FOLDER/pds-config.json

# Copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# Change owner of tool, workspace and pds folder as well as /run.sh
RUN chown --recursive gosec:gosec $TOOL_FOLDER $WORKSPACE $PDS_FOLDER /run.sh

WORKDIR $WORKSPACE

# switch from root to non-root user
USER gosec

CMD ["/run.sh"]
