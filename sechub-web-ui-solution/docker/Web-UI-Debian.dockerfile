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
    apt-get install -y git wget && \
    apt-get clean

RUN cd "${WEB_UI_BUILD_FOLDER}" && \
    chmod 755 clone.sh && \
    ./clone.sh "$GIT_URL" "$GIT_BRANCH" "$GIT_TAG" && \
    cd "sechub/sechub-web-ui" && \
    npm install && \
    npm run build && \
    cp -r dist "${WEB_UI_ARTIFACTS}"


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
    apt-get install -y unzip wget && \
    apt-get clean

RUN mkdir -p "${WEB_UI_ARTIFACTS}/dist" && \
    cd "${WEB_UI_ARTIFACTS}/dist" && \
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
    mkdir -p "$$HTDOCS_FOLDER" "$CERTIFICATE_DIRECTORY"

# Copy launcher script into container
COPY run.sh /run.sh

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get -y upgrade && \
    apt-get -y install bind9-host curl netcat-openbsd nginx openssl sed vim-tiny && \
    apt-get -y clean && \
    # Cleanup nginx default files
    cd /etc/nginx && \
    rm -rf conf.d fastcgi* koi-* modules-* *_params sites-* snippets win-utf /var/www/html/index.nginx-debian.html

# Copy Nginx configuration files
COPY nginx/ /etc/nginx/

# Copy content to web server's document root
COPY --from=builder "${WEB_UI_ARTIFACTS}/dist" "${HTDOCS_FOLDER}"
COPY htdocs/ "${HTDOCS_FOLDER}/"

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
RUN mv /tmp/sechub-web-ui.cert "$CERTIFICATE_DIRECTORY"/sechub-web-ui.cert && \
    mv /tmp/sechub-web-ui.key "$CERTIFICATE_DIRECTORY"/sechub-web-ui.key && \
    # Generate ephemeral Diffie-Hellman paramaters for perfect forward secrecy
    # see: https://raymii.org/s/tutorials/Strong_SSL_Security_On_nginx.html#toc_5
    openssl dhparam -out "$CERTIFICATE_DIRECTORY"/certsdhparam.pem 2048 2>&1 | sed 's/\.//g'

# Create PID file and set permissions
RUN touch /var/run/nginx.pid && \
    chmod 755 "$HTDOCS_FOLDER" && \
    chown -R "$USER:$USER" "$CERTIFICATE_DIRECTORY" /var/log/nginx /var/lib/nginx /var/run/nginx.pid && \
    chmod +x /run.sh

# Switch from root to non-root user
USER "$USER"

WORKDIR "$HTDOCS_FOLDER"

CMD ["/run.sh"]
