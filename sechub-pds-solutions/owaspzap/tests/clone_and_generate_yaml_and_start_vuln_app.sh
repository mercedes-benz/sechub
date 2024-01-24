#!/bin/bash
# SPDX-License-Identifier: MIT

current_test_folder="$1"
path_to_docker_compose_yaml_file="$2"
vulnerable_app_repo_to_clone="$3"
	
if [[ ! -d "$current_test_folder" ]]
then
	echo "Directory does not exist"
	exit 1
fi

if [[ -z "$path_to_docker_compose_yaml_file" ]]
then
	echo "No path to docker-compose yaml file provided"
	exit 1
fi

if [[ -z "$vulnerable_app_repo_to_clone" ]]
then
	echo "No vulnerable application repository provided"
	exit 1
fi

cd "$current_test_folder"

echo "cloning vulnerable app repo..."
git clone "$vulnerable_app_repo_to_clone"

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
    name: "sechub"' > "$path_to_docker_compose_yaml_file"

docker compose --file "$path_to_docker_compose_yaml_file" up go-test-bench -d --remove-orphans
