#!/bin/bash
# SPDX-License-Identifier: MIT

cd "$(dirname "$0")" || exit

resource_limits_enabled="$1"
compose_file="docker-compose_sechub-debian"

if [[ "$resource_limits_enabled" == "yes" ]]
then
    compose_file="docker-compose_sechub_resource_limits-debian"
fi

docker compose --file "$compose_file.yaml" down --remove-orphans