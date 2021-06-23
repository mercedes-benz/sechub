#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

ENVIRONMENT_FILE=".env"

if [[ ! -f  "$ENVIRONMENT_FILE" ]]
then
    echo "Environment file does not exist."
    echo "Creating default environment file $ENVIRONMENT_FILE for you."

    cat << SETTINGS > $ENVIRONMENT_FILE
START_MODE=localserver
ADMIN_USERID=admin
ADMIN_APITOKEN="{noop}pds-apitoken"
TECHUSER_USERID=techuser
TECHUSER_APITOKEN="{noop}pds-apitoken"
SETTINGS

else
    echo "Using existing environment file: $ENVIRONMENT_FILE."
fi

echo "Starting container."
docker-compose --file pds_gosec_ubuntu.yaml up --build

