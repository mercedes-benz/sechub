# SPDX-License-Identifier: MIT

FROM ubuntu:20.04

ENV STORAGE_USER=seaweedfs
ENV STORAGE_FOLDER=/storage
ENV SEAWEEDFS_VERSION=2.78
ENV SEAWEEDFS_CHECKSUM=eec03dadc81ffa8ee39c62f077ee8b35
ENV BUCKET_FOLDER=/buckets

# non-root user
# using fixed group and user ids
RUN addgroup --gid 9000 "$STORAGE_USER" \
     && adduser --uid 9000 --disabled-password --ingroup "$STORAGE_USER" "$STORAGE_USER"

# create storage directory
RUN mkdir "$STORAGE_FOLDER" && \
    chown -R "$STORAGE_USER" "$STORAGE_FOLDER" && \
    chmod u+rxw "$STORAGE_FOLDER"

# copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade  && \
    apt-get --assume-yes install wget && \
    apt-get --assume-yes clean

RUN mkdir -p $BUCKET_FOLDER && \
    chown -R "$STORAGE_USER:$STORAGE_USER" /buckets

RUN cd /tmp && \
    # download seaweedfs
    wget --no-verbose "https://github.com/chrislusf/seaweedfs/releases/download/$SEAWEEDFS_VERSION/linux_amd64.tar.gz" && \
    # create checksum file
    echo "$SEAWEEDFS_CHECKSUM  linux_amd64.tar.gz" > linux_amd64.tar.gz.md5sum && \
    # verify checksum
    md5sum -c linux_amd64.tar.gz.md5sum && \
    # extract seaweedfs executable
    tar -xvzf "linux_amd64.tar.gz" -C "/usr/local/bin/"

# switch from root to non-root user
USER $STORAGE_USER

CMD ["/run.sh"]
