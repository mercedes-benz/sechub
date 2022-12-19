#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

SCRIPT_DIR=`dirname $0`
REPLICAS="$1"

ENVIRONMENT_FILE=".env-cluster"

if [[ ! -f  "$ENVIRONMENT_FILE" ]]
then
    echo "Environment file does not exist."
    echo "Creating default environment file $ENVIRONMENT_FILE for you."

    cp "$SCRIPT_DIR/env-cluster-initial" "$SCRIPT_DIR/$ENVIRONMENT_FILE"
else
    echo "Using existing environment file: $ENVIRONMENT_FILE."
fi

if [[ -z "$REPLICAS" ]]
then
    echo "Starting single container."
    REPLICAS=1
else
    echo "Starting cluster of $REPLICAS containers."
fi

docker compose --file docker-compose_pds_pmd_cluster.yaml up --scale pds-pmd=$REPLICAS --build --remove-orphans
