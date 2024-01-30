#!/bin/bash
# SPDX-License-Identifier: MIT

CURRENT_TEST_FOLDER="$1"
PATH_TO_DOCKER_COMPOSE_YAML_FILE="$2"
VULNERABLE_APP_REPO_TO_CLONE="$3"
	
if [[ ! -d "$CURRENT_TEST_FOLDER" ]]
then
	echo "Directory does not exist"
	exit 1
fi

if [[ -z "$PATH_TO_DOCKER_COMPOSE_YAML_FILE" ]]
then
	echo "No path to docker-compose yaml file provided"
	exit 1
fi

if [[ -z "$VULNERABLE_APP_REPO_TO_CLONE" ]]
then
	echo "No vulnerable application repository provided"
	exit 1
fi

cd "$CURRENT_TEST_FOLDER"

echo "cloning vulnerable app repo..."
git clone "$VULNERABLE_APP_REPO_TO_CLONE"

echo 'version: "1"
services:
  go-test-bench:
    build: .
    container_name: go-test-bench
    hostname: go-test-bench
    networks:
      - "sechub"

networks:
  sechub:
    external: true
    name: "sechub"' > "$PATH_TO_DOCKER_COMPOSE_YAML_FILE"

docker compose --file "$PATH_TO_DOCKER_COMPOSE_YAML_FILE" up go-test-bench --detach --build --remove-orphans
