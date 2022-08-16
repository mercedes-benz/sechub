#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")
source "0000-helper.sh"

REPLICAS="$1"
ENVIRONMENT_FILE=".env-cluster"

resource_limits_enabled="$2"
compose_file="docker-compose_sechub_cluster"

setup_environment_file "$ENVIRONMENT_FILE" "env-initial" "env-initial-cluster"

if [[ "$resource_limits_enabled" == "yes" ]]
then
    compose_file="docker-compose_sechub_cluster_resource_limits"
fi

if [[ -z "$REPLICAS" ]]
then
    echo "Starting single container."
    REPLICAS=1
else
    echo "Starting cluster of $REPLICAS containers."
fi

# Use Docker BuildKit
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

docker-compose --file "$compose_file.yaml" up --scale sechub=$REPLICAS --build --remove-orphans