# SPDX-License-Identifier: MIT

FROM debian:12-slim

ARG POSTGRES_VERSION=13
ARG POSTGRES_CONFIG_FOLDER="/etc/postgresql/${POSTGRES_VERSION}/main/"

ENV POSTGRES_VERSION="${POSTGRES_VERSION}"

# Install PostgreSQL
RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get --assume-yes upgrade && \
    apt-get --assume-yes install postgresql postgresql-client && \
    apt-get --assume-yes clean

# PostgreSQL configuration files
COPY pg_hba.conf  "$POSTGRES_CONFIG_FOLDER/pg_hba.conf"
COPY postgresql.conf "$POSTGRES_CONFIG_FOLDER/postgresql.conf"

# Copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

# The postgres user was crated by the postgresql installation
USER postgres

CMD ["/run.sh"]