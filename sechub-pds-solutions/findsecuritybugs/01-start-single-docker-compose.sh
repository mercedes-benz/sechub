#!/bin/bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")
source "../../sechub-solutions-shared/scripts/9999-env-file-helper.sh"

ENVIRONMENT_FILES_FOLDER="../shared/environment"
ENVIRONMENT_FILE=".env-single"

# Only variables from .env can be used in the Docker-Compose file
# all other variables are only available in the container
setup_environment_file ".env" "env" "$ENVIRONMENT_FILES_FOLDER/env-base-image"
setup_environment_file "$ENVIRONMENT_FILE" "$ENVIRONMENT_FILES_FOLDER/env-base"

cp --recursive --force ../../sechub-solutions-shared/install-java/ docker/

# Use Docker BuildKit
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

echo "Starting single container."
docker compose --file docker-compose_pds_findsecuritybugs.yaml up --build --remove-orphans

