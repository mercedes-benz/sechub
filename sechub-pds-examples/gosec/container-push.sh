#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

if [[ -z "$1" ]]
then
  echo "Please provide the registry and image name."
  exit 1
fi

if [[ -z "$2" ]]
then
  echo "Please provide the version number."
  exit 1
fi

REGISTRY="$1"
VERSION="$2"

echo "*****************************"
echo " Publish Docker Image "
echo "*****************************"
echo "Pushing docker image: $REGISTRY:$VERSION"
docker push "$REGISTRY:$VERSION"
