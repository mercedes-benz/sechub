#!/bin/bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")
source "../sechub-solutions-shared/scripts/9999-env-file-helper.sh"

# Only variables from .env can be used in the Docker-Compose file
# all other variables are only available in the container
setup_environment_file ".env" "env"
setup_environment_file ".env-web-server" "env-web-server"

# Use Docker BuildKit
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

echo "Starting single container."
docker compose --file docker-compose_web_server_external-network.yaml up --build --remove-orphans
