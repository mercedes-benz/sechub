#!/bin/bash
# SPDX-License-Identifier: MIT
CURRENT_TEST_FOLDER="$1"
PATH_TO_DOCKER_COMPOSE_YAML_FILE="$2"
	
if [[ ! -d "$CURRENT_TEST_FOLDER" ]]
then
	echo "Target folder is empty"
	exit 1
fi

if [[ -z "$PATH_TO_DOCKER_COMPOSE_YAML_FILE" ]]
then
	echo "No path to docker-compose yaml file provided"
	exit 1
fi

cd "$CURRENT_TEST_FOLDER"

docker compose --file "$PATH_TO_DOCKER_COMPOSE_YAML_FILE" down --remove-orphans
