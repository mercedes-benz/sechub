#!/bin/bash
# SPDX-License-Identifier: MIT
current_test_folder="$1"
path_to_docker_compose_yaml_file="$2"
	
if [[ ! -d "$current_test_folder" ]]
then
	echo "Target folder is empty"
	exit 1
fi

if [[ -z "$path_to_docker_compose_yaml_file" ]]
then
	echo "No path to docker-compose yaml file provided"
	exit 1
fi

cd "$current_test_folder"

docker compose --file "$path_to_docker_compose_yaml_file" down --remove-orphans
