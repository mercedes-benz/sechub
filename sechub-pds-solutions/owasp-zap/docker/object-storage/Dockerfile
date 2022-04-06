# SPDX-License-Identifier: MIT

FROM alpine:edge

ENV STORAGE_FOLDER=/storage
ENV SEAWEEDFS_VERSION=2.97
ENV SEAWEEDFS_CHECKSUM=74e6b1e2928ef4a6162ab2072715ee08
ENV USER=seaweedfs

# non-root user
# using fixed group and user ids
RUN addgroup --gid 9000 "$USER" \
     && adduser --uid 9000 --disabled-password --ingroup "$USER" "$USER"

# create storage directory
RUN mkdir "$STORAGE_FOLDER" && \
    chown -R "$USER" "$STORAGE_FOLDER" && \
    chmod u+rxw "$STORAGE_FOLDER"

# copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# upgrade system, install s3cmd server and client
RUN apk update --no-cache && \
    apk add --no-cache wget && \
    # install s3cmd client from the alpine testing repository
    apk add --no-cache -X http://dl-cdn.alpinelinux.org/alpine/edge/testing s3cmd

RUN cd /tmp && \
    # download seaweedfs
    wget "https://github.com/chrislusf/seaweedfs/releases/download/$SEAWEEDFS_VERSION/linux_amd64.tar.gz" && \
    # create checksum file
    echo "$SEAWEEDFS_CHECKSUM  linux_amd64.tar.gz" > linux_amd64.tar.gz.md5sum && \
    # verify checksum
    md5sum -c linux_amd64.tar.gz.md5sum && \
    # extract seaweedfs executable
    tar -xvzf "linux_amd64.tar.gz" -C "/usr/local/bin/"

# switch from root to non-root user
USER $USER

CMD ["/run.sh"]
