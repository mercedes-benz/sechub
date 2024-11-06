# SPDX-License-Identifier: MIT

#-------------------
# Global Variables
#-------------------

# The image argument needs to be placed on top
ARG BASE_IMAGE
ARG NODE_VERSION="22.9.0"
ARG NODE_BASE_IMAGE="node:${NODE_VERSION}-slim"
ARG WEB_UI_ARTIFACTS="/artifacts"

# Build args
ARG WEB_UI_VERSION
ARG BUILD_TYPE


#-------------------
# Builder Build
#-------------------
FROM ${NODE_BASE_IMAGE} AS builder-build
ARG GIT_URL="https://github.com/mercedes-benz/sechub.git"
ARG GIT_BRANCH
ARG GIT_TAG
ARG WEB_UI_BUILD_FOLDER="/build"
ARG WEB_UI_ARTIFACTS

RUN mkdir --parent "${WEB_UI_ARTIFACTS}" "${WEB_UI_BUILD_FOLDER}"

COPY clone.sh "$WEB_UI_BUILD_FOLDER/clone.sh"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install --assume-yes --quiet git wget && \
    apt-get clean

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
FROM ${BASE_IMAGE} AS builder-copy
ARG WEB_UI_ARTIFACTS

RUN mkdir --parent "${WEB_UI_ARTIFACTS}"

COPY copy/ "${WEB_UI_ARTIFACTS}"


#-----------------------
# Builder Download Build
#-----------------------
FROM ${BASE_IMAGE} AS builder-download
ARG WEB_UI_ARTIFACTS
ARG WEB_UI_VERSION

ENV WEB_UI_VERSION="${WEB_UI_VERSION}"
ENV WEB_UI_RELEASE_ZIP="sechub-web-ui_htdocs.zip"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install --assume-yes --quiet unzip wget && \
    apt-get clean

RUN mkdir -p "${WEB_UI_ARTIFACTS}/.output/public" && \
    cd "${WEB_UI_ARTIFACTS}/.output/public" && \
    wget "https://github.com/mercedes-benz/sechub/releases/download/v${WEB_UI_VERSION}-web-ui/${WEB_UI_RELEASE_ZIP}" && \
    wget "https://github.com/mercedes-benz/sechub/releases/download/v${WEB_UI_VERSION}-web-ui/${WEB_UI_RELEASE_ZIP}.sha256sum" && \
    sha256sum --check "${WEB_UI_RELEASE_ZIP}.sha256sum" && \
    unzip ${WEB_UI_RELEASE_ZIP} && \
    rm -f "${WEB_UI_RELEASE_ZIP}" "${WEB_UI_RELEASE_ZIP}.sha256sum"
    
    
#-------------------
# Builder
#-------------------
FROM builder-${BUILD_TYPE} AS builder
RUN echo "build stage"


#-------------------
# WebUI Server Image
#-------------------
FROM ${BASE_IMAGE} AS web-ui
ARG HTDOCS_FOLDER="/var/www/html"
ARG USER=www-data
ARG WEB_UI_ARTIFACTS
ARG WEB_UI_VERSION

# env vars in container
ENV UID="4242"
ENV GID="${UID}"
ENV CERTIFICATE_DIRECTORY="/etc/nginx/certificates"
ENV LOADBALANCER_START_MODE="server"
ENV WEB_UI_VERSION="${WEB_UI_VERSION}"
ENV HTDOCS_FOLDER="${HTDOCS_FOLDER}"

# using fixed group and user ids + prepare alive check file
RUN usermod -u "$UID" "$USER" && \
    groupmod -g "$GID" "$USER" && \
    NGINX_ALIVE_DIR="$HTDOCS_FOLDER/health" && \
    mkdir -p "$NGINX_ALIVE_DIR" && \
    echo "SecHub Web-UI is alive" > "$NGINX_ALIVE_DIR/alive.html"

# Copy run script into container
COPY run.sh /run.sh

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade && \
    apt-get --assume-yes install nginx openssl sed && \
    apt-get --assume-yes clean

# Copy configuration script
COPY nginx.conf /etc/nginx/nginx.conf

# Create self-signed certificate
RUN cd /tmp && \
    openssl req \
        -new \
        -newkey rsa:2048 \
        -days 365 \
        -nodes \
        -x509 \
        -subj "/C=DE/ST=BW/L=Stuttgart/O=Loadbalancer/CN=localhost" \
        -keyout sechub-web-ui.key \
        -out sechub-web-ui.cert \
    2>&1 | sed 's/\.//g'

# Prepare certificates
RUN mkdir -p "$CERTIFICATE_DIRECTORY" && \
    mv /tmp/sechub-web-ui.cert "$CERTIFICATE_DIRECTORY"/sechub-web-ui.cert && \
    mv /tmp/sechub-web-ui.key "$CERTIFICATE_DIRECTORY"/sechub-web-ui.key && \
    # Generate ephemeral Diffie-Hellman paramaters for perfect forward secrecy
    # see: https://raymii.org/s/tutorials/Strong_SSL_Security_On_nginx.html#toc_5
    openssl dhparam -out "$CERTIFICATE_DIRECTORY"/certsdhparam.pem 2048 2>&1 | sed 's/\.//g'

# Copy content to web server's document root
COPY --from=builder "${WEB_UI_ARTIFACTS}/.output/public" "${HTDOCS_FOLDER}"

# Create PID file and set permissions
RUN touch /var/run/nginx.pid && \
    chmod 755 "$HTDOCS_FOLDER" && \
    chown -R "$USER:$USER" "$CERTIFICATE_DIRECTORY" "$HTDOCS_FOLDER" /var/log/nginx /var/lib/nginx /etc/nginx/conf.d /var/run/nginx.pid && \
    chmod +x /run.sh

# Switch from root to non-root user
USER "$USER"

WORKDIR "$HTDOCS_FOLDER"

CMD ["/run.sh"]
