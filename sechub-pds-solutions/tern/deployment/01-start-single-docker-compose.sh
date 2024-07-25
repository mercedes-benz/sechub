#!/usr/bin/bash
# SPDX-License-Identifier: MIT

ENVIRONMENT_FILE=".env"

if [[ ! -f  "$ENVIRONMENT_FILE" ]]
then
  echo "Environment file does not exist."
  echo "Creating default environment file $ENVIRONMENT_FILE for you."
        
   cat "env" > "$ENVIRONMENT_FILE"
else
  echo "Using existing environment file: $environment_file."
fi

# Use Docker BuildKit
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

echo "Starting single container and detaching it from shell."
nohup docker compose --file docker-compose_pds_tern.yaml up --build --remove-orphans &
