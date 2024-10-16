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
# Builder Copy Build
#-------------------

FROM ${BASE_IMAGE} AS builder-copy
ARG WEB_UI_FOLDER="/var/www/html/web-ui"

RUN mkdir --parent "${WEB_UI_FOLDER}"

COPY ./copy "${WEB_UI_FOLDER}"


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
