#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

REPLICAS="$1"

cd $(dirname "$0")
source "../../sechub-solutions-shared/scripts/9999-env-file-helper.sh"

ENVIRONMENT_FILES_FOLDER="../shared/environment"
ENVIRONMENT_FILE=".env-cluster-object-storage"

# Only variables from .env can be used in the Docker-Compose file
# all other variables are only available in the container
setup_environment_file ".env" "env"
setup_environment_file "$ENVIRONMENT_FILE" "$ENVIRONMENT_FILES_FOLDER/env-base" "$ENVIRONMENT_FILES_FOLDER/env-cluster" "$ENVIRONMENT_FILES_FOLDER/env-object-storage" "env-database"


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

docker compose --file docker-compose_pds_iac_cluster_object_storage.yaml up --scale pds-iac=$REPLICAS --build --remove-orphans