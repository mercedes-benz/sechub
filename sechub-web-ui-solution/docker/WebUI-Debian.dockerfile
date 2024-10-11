# SPDX-License-Identifier: MIT

#-------------------
# Global Variables
#-------------------

# The image argument needs to be placed on top
ARG NODE_VERSION
ARG BASE_IMAGE=node:${NODE_VERSION}-slim

# Build args
ARG WEBUI_VERSION
ARG BUILD_TYPE

#-------------------
# Builder Copy Build
#-------------------

FROM ${BASE_IMAGE} AS builder-copy

ARG WEBUI_VERSION

RUN mkdir --parent "$WEBUI_FOLDER"

COPY ./copy "$WEBUI_FOLDER"


# env vars in container
ENV USER="webui"
ENV UID="4242"
ENV GID="${UID}"
ENV WEBUI_VERSION="${WEBUI_VERSION}"
ENV WEBUI_FOLDER="/sechub-webui"

# env for Vue.js and Nuxt
ENV PORT="${WEBUI_PORT}"
ENV HOST="${WEBUI_HOST}"
ENV NODE_ENV=${NODE_ENV}

# non-root user
# using fixed group and user ids
RUN groupadd --gid "$GID" "$USER" && \
    useradd --uid "$UID" --gid "$GID" --no-log-init --create-home "$USER"

# Copy run script into the container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# Set permissions
RUN chown --recursive "$USER:$USER" "$WEBUI_FOLDER"

# Set workspace
WORKDIR "$WEBUI_FOLDER"

# Switch from root to non-root user
USER "$USER"

CMD ["/run.sh"]
