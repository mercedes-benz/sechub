#!/bin/bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")
source "../sechub-solutions-shared/scripts/9999-env-file-helper.sh"

# Only variables from .env can be used in the Docker-Compose file
# all other variables are only available in the container
setup_environment_file ".env" "env"
setup_environment_file ".env-web-server" "env-web-server"

# Use Docker BuildKit
# nesessary for switching between build stages
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

echo "Copying install-java scripts into the docker directory"
cp --recursive --force ../sechub-solutions-shared/install-java/ docker/

echo "Starting single container."
docker compose --file docker-compose_web_server.yaml up --build --remove-orphans
