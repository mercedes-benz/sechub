#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

resource_limits_enabled="$1"
shift
if [ -z "$1" ]; then
    compose_file="docker-compose_sechub"
else
    compose_file="$1"
fi

if [[ "$resource_limits_enabled" == "yes" ]]
then
    compose_file="docker-compose_sechub_resource_limits"
fi

docker compose --file "$compose_file.yaml" down --remove-orphans