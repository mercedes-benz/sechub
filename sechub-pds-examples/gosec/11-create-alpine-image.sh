#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

REGISTRY="$1"
VERSION="$2"
BASE_IMAGE="$3"

if [[ -z "$REGISTRY" ]]
then
  echo "Please provide a docker registry server."
  exit 1
fi

if [[ -z "$VERSION" ]]
then
  echo "Please provide a version for the container."
  exit 1
fi

if [[ -z "$BASE_IMAGE" ]]
then
    BASE_IMAGE="alpine:3.14"
fi

echo ">> Base image: $BASE_IMAGE"
docker build --no-cache --build-arg BASE_IMAGE=$BASE_IMAGE --tag "$REGISTRY:$VERSION" --file context/Dockerfile-Alpine context/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"

