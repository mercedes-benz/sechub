#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

ENVIRONMENT_FILE=".env-single"

resource_limits_enabled="$1"
compose_file="docker-compose_sechub-debian-prepare"

cd $(dirname "$0")
source ../sechub-solutions-shared/scripts/9999-env-file-helper.sh

# Only variables from .env can be used in the Docker-Compose file
# all other variables are only available in the container
setup_environment_file ".env" "env"
setup_environment_file "$ENVIRONMENT_FILE" "env-sechub"

echo "Copying install-java scripts into the docker directory"
cp --recursive --force ../sechub-solutions-shared/install-java/ docker/

# Use Docker BuildKit
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

if [[ "$resource_limits_enabled" == "yes" ]]
then
    compose_file="docker-compose_sechub_resource_limits-debian"
fi

echo "Compose file: $compose_file"

docker compose --file "$compose_file.yaml" up --build --remove-orphans
