#!/bin/bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")
source "../../sechub-solutions-shared/scripts/9999-env-file-helper.sh"

ENVIRONMENT_FILES_FOLDER="../shared/environment"
ENVIRONMENT_FILE=".env-single"

network="internal"
dockerfile="docker-compose_gitleaks.yaml"

if [[ ! -z "$1" ]]
then
    network="$1"
fi

# Only variables from .env can be used in the Docker-Compose file
# all other variables are only available in the container
setup_environment_file ".env" "env" "$ENVIRONMENT_FILES_FOLDER/env-base-image" "$ENVIRONMENT_FILES_FOLDER/env-port"
setup_environment_file "$ENVIRONMENT_FILE" "$ENVIRONMENT_FILES_FOLDER/env-base"

# Use Docker BuildKit
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

if [[ "$network" == "external" ]]
then
    echo "Starting single container with external network."
    dockerfile="docker-compose_pds_gitleaks_external-network.yaml"
else
    echo "Starting single container."
fi

docker compose --file "$dockerfile" up --build --remove-orphans
