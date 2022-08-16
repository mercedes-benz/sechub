#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")
source "0000-helper.sh"

REPLICAS="$1"
ENVIRONMENT_FILE=".env-cluster-object-storage"

if [[ ! -f  "$ENVIRONMENT_FILE" ]]
then
    echo "Environment file does not exist."
    echo "Creating default environment file $ENVIRONMENT_FILE for you."

    cat "env-initial" "env-initial-cluster" "env-initial-cluster-object-storage" > "$ENVIRONMENT_FILE"
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

docker-compose --file docker-compose_pds_gosec_cluster_object_storage.yaml up --scale pds-gosec=$REPLICAS --build --remove-orphans