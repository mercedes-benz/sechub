# SPDX-License-Identifier: MIT

FROM ubuntu:20.04

ENV POSTGRE_CONFIG_FOLDER="/etc/postgresql/12/main/"
ENV USER=postgres

# install PostgreSQL
RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get upgrade --assume-yes && \
    apt-get install --assume-yes postgresql postgresql-client && \
    apt-get clean

# PostgreSQL configuration files
RUN rm "$POSTGRE_CONFIG_FOLDER/pg_hba.conf"
COPY pg_hba.conf  "$POSTGRE_CONFIG_FOLDER/pg_hba.conf"

RUN rm "$POSTGRE_CONFIG_FOLDER/postgresql.conf"
COPY postgresql.conf "$POSTGRE_CONFIG_FOLDER/postgresql.conf"

# copy run script into container
COPY run.sh /run.sh
RUN chmod +x /run.sh

USER "$USER"

CMD ["/run.sh"]