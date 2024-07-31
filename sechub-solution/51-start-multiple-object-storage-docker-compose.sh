#!/usr/bin/bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")
source ../sechub-solutions-shared/scripts/9999-env-file-helper.sh

REPLICAS="$1"
ENVIRONMENT_FILE=".env-cluster-object-storage"

resource_limits_enabled="$2"
compose_file="docker-compose_sechub_cluster_object_storage"

# Only variables from .env can be used in the Docker-Compose file
# all other variables are only available in the container
setup_environment_file ".env" "env"
setup_environment_file "$ENVIRONMENT_FILE" "env-sechub" "env-cluster" "env-object-storage"

if [[ "$resource_limits_enabled" == "yes" ]]
then
    compose_file="docker-compose_sechub_cluster_object_storage_resource_limits"
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

echo "Compose file: $compose_file"
docker compose --file "$compose_file.yaml" up --scale sechub=$REPLICAS --build --remove-orphans