# SPDX-License-Identifier: MIT

#-------------------
# Global Variables
#-------------------

# The image argument needs to be placed on top
ARG NODE_VERSION
ARG NODE_BASE_IMAGE=node:${NODE_VERSION}-slim
ARG BASE_IMAGE

# Build args
ARG WEB_UI_VERSION
ARG BUILD_TYPE

ARG NODE_ENV
#-------------------
# Builder Build
#-------------------

FROM ${NODE_BASE_IMAGE} AS builder-build
ARG GIT_URL="https://github.com/mercedes-benz/sechub.git"
ARG GIT_BRANCH
ARG GIT_TAG
ARG WEB_UI_BUILD_FOLDER="/build"
ARG WEB_UI_ARTIFACTS="/artifacts"

RUN mkdir --parent "${WEB_UI_ARTIFACTS}"
RUN mkdir --parent "${WEB_UI_BUILD_FOLDER}"

RUN apt-get update && \
    apt-get upgrade --assume-yes --quiet && \
    apt-get install --assume-yes --quiet git wget && \
    apt-get clean

COPY clone.sh "$WEB_UI_BUILD_FOLDER/clone.sh"

RUN cd "${WEB_UI_BUILD_FOLDER}" && \
    chmod 755 clone.sh && \
    ./clone.sh "$GIT_URL" "$GIT_BRANCH" "$GIT_TAG" && \
    cd "sechub/sechub-web-ui" && \
    npm install && \
    npx nuxi generate && \
    cp -r .output "${WEB_UI_ARTIFACTS}"

#-------------------
# Builder Copy Build
#-------------------

FROM ${NODE_BASE_IMAGE} AS builder-copy
ARG WEB_UI_ARTIFACTS="/artifacts"

RUN mkdir --parent "${WEB_UI_ARTIFACTS}"

COPY ./copy "${WEB_UI_ARTIFACTS}"

#-------------------
# Builder
#-------------------

FROM builder-${BUILD_TYPE} as builder
RUN echo "build stage"

#-------------------
# WebUI Server Image
#-------------------

FROM ${BASE_IMAGE} AS web-ui
ARG USER=www-data
ARG WEB_UI_ARTIFACTS="/artifacts"
ARG WEB_UI_FOLDER="/var/www/html/"

COPY --from=builder "${WEB_UI_ARTIFACTS}/.output/public" "${WEB_UI_FOLDER}"

# env vars in container
ENV UID="4242"
ENV GID="${UID}"
ENV WEB_UI_VERSION="${WEB_UI_VERSION}"
ENV WEB_UI_FOLDER="${WEB_UI_FOLDER}"

# non-root user
# using fixed group and user ids
RUN usermod -u "$UID" "$USER" && \
    groupmod -g "$GID" "$USER"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade && \
    apt-get --assume-yes install nginx openssl sed && \
    apt-get --assume-yes clean

# Create self-signed certificate
RUN cd /tmp && \
    openssl req \
        -new \
        -newkey rsa:2048 \
        -days 365 \
        -nodes \
        -x509 \
        -subj "/C=DE/ST=BW/L=Stuttgart/O=Loadbalancer/CN=localhost" \
        -keyout localhost.key \
        -out localhost.cert

# Certificates
RUN mkdir -p /certificates && \
    mv /tmp/localhost.cert /certificates/localhost.cert && \
    mv /tmp/localhost.key /certificates/localhost.key

# Generate ephemeral Diffie-Hellman paramaters for perfect forward secrecy
# see: https://raymii.org/s/tutorials/Strong_SSL_Security_On_nginx.html#toc_5
RUN openssl dhparam -out /certificates/certsdhparam.pem 2048

# Copy configuration script
COPY nginx.conf /etc/nginx/nginx.conf

# Create PID file and set permissions
RUN touch /var/run/nginx.pid && \
    chmod 755 ${WEB_UI_FOLDER} && \
    chown -R "$USER:$USER" /certificates /var/log/nginx /var/lib/nginx /etc/nginx/conf.d /var/run/nginx.pid ${WEB_UI_FOLDER}

# Copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

ENV LOADBALANCER_START_MODE=server

# Switch from root to non-root user
USER "$USER"

CMD ["/run.sh"]
