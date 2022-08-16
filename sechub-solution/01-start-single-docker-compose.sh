#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

ENVIRONMENT_FILE=".env"

resource_limits_enabled="$1"
compose_file="docker-compose_sechub"

cd $(dirname "$0")
source "0000-helper.sh"

setup_environment_file "$ENVIRONMENT_FILE" "env-initial"

# Use Docker BuildKit
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

if [[ "$resource_limits_enabled" == "yes" ]]
then
    compose_file="docker-compose_sechub_resource_limits"
fi

docker-compose --file "$compose_file.yaml" up --build --remove-orphans