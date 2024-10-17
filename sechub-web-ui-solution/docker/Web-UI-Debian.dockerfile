# SPDX-License-Identifier: MIT

#-------------------
# Global Variables
#-------------------

# The image argument needs to be placed on top
ARG NODE_VERSION
ARG BASE_IMAGE=node:${NODE_VERSION}-slim

# Build args
ARG WEB_UI_VERSION
ARG BUILD_TYPE

ARG NODE_ENV
#-------------------
# Builder Build
#-------------------

FROM ${BASE_IMAGE} AS builder-build
ARG WEB_UI_BUILD_FOLDER="/build"
ARG WEB_UI_ARTIFACTS="/artifacts"

ARG GIT_URL="https://github.com/mercedes-benz/sechub.git"
ARG BRANCH
ARG TAG

RUN mkdir --parent "${WEB_UI_ARTIFACTS}"
RUN mkdir --parent "${WEB_UI_BUILD_FOLDER}"

RUN apt-get update && \
    apt-get upgrade --assume-yes --quiet && \
    apt-get install --assume-yes --quiet git wget && \
    apt-get clean

COPY clone.sh "$WEB_UI_BUILD_FOLDER/clone.sh"

RUN cd "${WEB_UI_BUILD_FOLDER}" && \
    chmod 755 clone.sh && \
    ./clone.sh "$GIT_URL" "$BRANCH" "$TAG" && \
    cd "sechub/sechub-web-ui" && \
    npm install && \
    npm run build && \
    cp -r .output "${WEB_UI_ARTIFACTS}" && \
    rm -rf "${WEB_UI_BUILD_FOLDER}"

#-------------------
# Builder Copy Build
#-------------------

FROM ${BASE_IMAGE} AS builder-copy
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
ARG WEB_UI_ARTIFACTS="/artifacts"
ARG WEB_UI_FOLDER="/var/www/html/web-ui"

COPY --from=builder "${WEB_UI_ARTIFACTS}" "${WEB_UI_FOLDER}"

# env vars in container
ENV USER="web-ui"
ENV UID="4242"
ENV GID="${UID}"
ENV WEB_UI_VERSION="${WEB_UI_VERSION}"
ENV WEB_UI_FOLDER="${WEB_UI_FOLDER}"

# env for Vue.js and Nuxt
ENV PORT="${WEB_UI_PORT}"
ENV HOST="${WEB_UI_HOST}"
ENV NODE_ENV=${NODE_ENV}

# non-root user
# using fixed group and user ids
RUN groupadd --gid "$GID" "$USER" && \
    useradd --uid "$UID" --gid "$GID" --no-log-init --create-home "$USER"

# Copy run script into the container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# Set permissions
RUN chown --recursive "$USER:$USER" "$WEB_UI_FOLDER"

# Set workspace
WORKDIR "$WEB_UI_FOLDER"

# Switch from root to non-root user
USER "$USER"

CMD ["/run.sh"]
