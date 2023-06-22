#!/bin/bash
# SPDX-License-Identifier: MIT

cd `dirname $0`

REGISTRY="$1"
VERSION="$2"
BASE_IMAGE="$3"

usage() {
  cat - <<EOF
usage: $0 <docker registry> <version tag> <base image>
Builds a docker image of SecHub PDS with multiple scan tools
for <docker registry> with tag <version tag>.
Required: <base image> ; for example ghcr.io/mercedes-benz/sechub/pds-base
EOF
}

FAILED=false
if [[ -z "$REGISTRY" ]]
then
  echo "Please provide a docker registry server."
  FAILED=true
fi

if [[ -z "$VERSION" ]]
then
  echo "Please provide a version for the container."
  FAILED=true
fi

if [[ -z "$BASE_IMAGE" ]]
then
  echo "Please provide a PDS base image as 3rd parameter."
  FAILED=true
fi

if $FAILED ; then
  usage
  exit 1
fi

echo ">> Base image: $BASE_IMAGE"
docker build --pull --no-cache --build-arg BASE_IMAGE=$BASE_IMAGE --tag "$REGISTRY:$VERSION" --file docker/Multi-Debian.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
