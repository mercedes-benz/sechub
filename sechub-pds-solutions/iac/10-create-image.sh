#!/bin/bash
# SPDX-License-Identifier: MIT

cd `dirname $0`

REGISTRY="$1"
VERSION="$2"
BASE_IMAGE="$3"

usage() {
  cat - <<EOF

usage: $0 <docker registry> <version tag> <base image>
Builds a docker image of SecHub PDS with IaC
for <docker registry> with tag <version tag>.
Required: <base image> ; for example ghcr.io/mercedes-benz/sechub/pds-base:v0.32.1

Additionally these environment variables can be defined:
- KICS_VERSION - KICS version to use. E.g. 2.0.0
EOF
}

FAILED=false
if [[ -z "$REGISTRY" ]] ; then
  echo "Please provide a docker registry server as 1st parameter."
  FAILED=true
fi

if [[ -z "$VERSION" ]] ; then
  echo "Please provide a version for the container as 2nd parameter."
  FAILED=true
fi

if [[ -z "$BASE_IMAGE" ]]; then
  echo "Please provide a base image as 3rd parameter."
  FAILED=true
fi

if $FAILED ; then
  usage
  exit 1
fi

BUILD_ARGS="--build-arg BASE_IMAGE=$BASE_IMAGE"
echo ">> Base image: $BASE_IMAGE"

if [ -z "$KICS_VERSION" ] ; then
  # source defaults
  source ./env
fi
echo ">> KICS version: $KICS_VERSION"
BUILD_ARGS="$BUILD_ARGS --build-arg KICS_VERSION=$KICS_VERSION"

# Use Docker BuildKit
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

echo "docker build --pull --no-cache $BUILD_ARGS --tag "$REGISTRY:$VERSION" --file docker/IaC-Debian.dockerfile docker/"
docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file docker/IaC-Debian.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
