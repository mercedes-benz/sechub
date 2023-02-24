#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

yamlFile=$1

if [ -z "$yamlFile" ]; then
    docker compose --file docker-compose_pds_gosec_external-network.yaml down --remove-orphans
else
    docker compose --file "$yamlFile" down --remove-orphans
fi