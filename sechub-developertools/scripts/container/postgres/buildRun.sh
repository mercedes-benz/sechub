#!/usr/bin/env bash

exposed_port="$1"


image_name="sechub-test-postgres"
container_name="${image_name}_${exposed_port}"

docker build -t $image_name -f Dockerfile .

buildResult=$?

if [[ "$buildResult" != 0 ]]
then
  exit 1
fi

# -p host:port:container_port
docker run --rm -p 127.0.0.1:$exposed_port:5432/tcp -e DATABASE_START_MODE=server --name $container_name $image_name
