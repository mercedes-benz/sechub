# SPDX-License-Identifier: MIT

FROM alpine:edge

# non-root user
# using fixed group and user ids
# gosec needs a home directory for the cache
RUN addgroup --gid 9000 minio \
     && adduser --uid 9000 --disabled-password --ingroup minio minio

# create storage directory
RUN mkdir /storage && \
    chown -R minio /storage && \
    chmod u+rxw /storage

# copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# upgrade system, install Minio server and client
RUN apk update --no-cache && \
    # install Minio Server and client from the alpine testing repository
    apk add --no-cache -X http://dl-cdn.alpinelinux.org/alpine/edge/testing minio minio-client

# switch from root to non-root user
USER minio

CMD ["/run.sh"]
