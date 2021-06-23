#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

REGISTRY="$1"
VERSION="$2"
PUSH_LATEST="$3"

if [[ -z "$REGISTRY" ]]
then
  echo "Please provide the registry and image name."
  exit 1
fi

if [[ -z "$VERSION" ]]
then
  echo "Please provide the version number."
  exit 1
fi

echo "*****************************"
echo " Publish Docker Image "
echo "*****************************"
echo "Pushing docker image: $REGISTRY:$VERSION"
docker push "$REGISTRY:$VERSION"

if [[ "$PUSH_LATEST" == "yes" ]]
then
    echo "Pushing docker image: $REGISTRY:latest"
    docker push "$REGISTRY:latest"
fi
