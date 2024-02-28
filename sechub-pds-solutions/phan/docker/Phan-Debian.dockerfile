# SPDX-License-Identifier: MIT

# The image argument needs to be placed on top
ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# The remaining arguments need to be placed after the `FROM`
# See: https://ryandaniels.ca/blog/docker-dockerfile-arg-from-arg-trouble/

LABEL org.opencontainers.image.source="https://github.com/mercedes-benz/sechub"
LABEL org.opencontainers.image.title="SecHub Phan+PDS Image"
LABEL org.opencontainers.image.description="A container which combines Phan with the SecHub Product Delegation Server (PDS)"
LABEL maintainer="SecHub FOSS Team"

USER root

# Build Args
ARG PHAN_VERSION="5.4.3"
ARG PHP_VERSION="8.2"

# Copy mock folder
COPY mocks "$MOCK_FOLDER"

# Copy scripts
COPY scripts "$SCRIPT_FOLDER"
RUN chmod --recursive +x "$SCRIPT_FOLDER"

# Copy PDS configfile
COPY pds-config.json "$PDS_FOLDER/pds-config.json"

# Copy and modify composer.json
COPY composer.json "$PDS_FOLDER/composer.json"
RUN sed -i "s/PHAN_VERSION/${PHAN_VERSION}/g" "$PDS_FOLDER/composer.json"

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade && \
    apt-get --assume-yes install composer php-pear php${PHP_VERSION}-dev && \
    apt-get --assume-yes clean

# Install Phan
RUN composer require phan/phan

#Install php-ast extension
RUN pecl install ast
RUN echo "" >> /etc/php/${PHP_VERSION}/cli/php.ini
RUN echo "extension=ast.so" >> /etc/php/${PHP_VERSION}/cli/php.ini

# Create Phan config folder
RUN mkdir .phan

# Copy Phan config file
COPY config.php .phan/config.php
RUN chmod --recursive 777 .phan

# Set workspace
WORKDIR "$WORKSPACE"

# Switch from root to non-root user
USER "$USER"
