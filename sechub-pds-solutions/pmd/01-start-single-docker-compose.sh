#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

SCRIPT_DIR=`dirname $0`

ENVIRONMENT_FILE=".env"

if [[ ! -f  "$ENVIRONMENT_FILE" ]]
then
    echo "Environment file does not exist."
    echo "Creating default environment file $ENVIRONMENT_FILE for you."

    cp "$SCRIPT_DIR/env-initial" "$SCRIPT_DIR/$ENVIRONMENT_FILE"
else
    echo "Using existing environment file: $ENVIRONMENT_FILE."
fi

echo "Starting single container."
docker compose --file docker-compose_pds_pmd.yaml up --build --remove-orphans
