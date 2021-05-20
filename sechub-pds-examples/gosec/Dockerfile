FROM ubuntu:20.04

ARG WORKSPACE="/workspace"
ARG PDS_FOLDER="/pds"
ARG TOOL_FOLDER="/tool"

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update
RUN apt-get dist-upgrade --assume-yes
RUN apt-get install --assume-yes wget openjdk-11-jre-headless

# Install Go
RUN cd /tmp && \
    # create checksum file
    echo "013a489ebb3e24ef3d915abe5b94c3286c070dfe0818d5bca8108f1d6e8440d2 go1.16.linux-amd64.tar.gz" > go1.16.linux-amd64.tar.gz.sha256sum && \
    # download Go
    wget https://dl.google.com/go/go1.16.linux-amd64.tar.gz && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check go1.16.linux-amd64.tar.gz.sha256sum && \
    # extract Go
    tar -xvf go1.16.linux-amd64.tar.gz --directory /usr/local/ && \
    # add Go to path
    echo 'export PATH="$PATH:/usr/local/go/bin"' >> /root/.bashrc

# Install GoSec
RUN cd /tmp && \
    # download gosec
    wget https://github.com/securego/gosec/releases/download/v2.7.0/gosec_2.7.0_linux_amd64.tar.gz && \
    # download checksum
    wget https://github.com/securego/gosec/releases/download/v2.7.0/gosec_2.7.0_checksums.txt && \
    # verify checksum
    sha256sum --check --ignore-missing gosec_2.7.0_checksums.txt && \
    # unpack gosec
    tar --extract --ungzip --file gosec_2.7.0_linux_amd64.tar.gz --directory /bin/

# Install the Product Delegation Server (PDS)
RUN mkdir -p pds && \
    cd /pds && \
    # create checksum file
    echo "5337a7b4e30bb9dd24f63625c3563925e3a2be2bdc210b6f01f25aec0e9704bb  sechub-pds-0.20.0.jar" > sechub-pds-0.20.0.jar.sha256sum && \
    # download pds
    wget https://github.com/Daimler/sechub/releases/download/v0.20.0-pds/sechub-pds-0.20.0.jar && \
    # verify that the checksum and the checksum of the file are same
    sha256sum --check sechub-pds-0.20.0.jar.sha256sum

# Copy PDS configfile
COPY pds-config.json /$PDS_FOLDER/pds-config.json

# Create scripts folder
RUN mkdir -p $TOOL_FOLDER
COPY gosec.sh $TOOL_FOLDER/gosec.sh
RUN chmod +x $TOOL_FOLDER/gosec.sh

# Create the PDS workspace
RUN mkdir -p $WORKSPACE

COPY run.sh /run.sh
RUN chmod +x /run.sh

WORKDIR $WORKSPACE

CMD ["/run.sh"]
