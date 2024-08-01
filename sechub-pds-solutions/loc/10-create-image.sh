#!/bin/bash
# SPDX-License-Identifier: MIT

REGISTRY="$1"
VERSION="$2"
BASE_IMAGE="$3"

usage() {
  cat - <<EOF
usage: $0 <docker registry> <version tag> <base image>
Builds a docker image of SecHub PDS with loc analytics tools
for <docker registry> with tag <version tag>.
Required: <base image> ; for example ghcr.io/mercedes-benz/sechub/pds-base

Additionally these environment variables can be defined:
- CLOC_VERSION - Cloc version to use. E.g. 1.94
- SCC_VERSION - Scc version to use. E.g. 3.1.0

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

if [[ -z "$CLOC_VERSION" ]] ; then
  # source defaults
  source ./env
fi
echo ">> Cloc version: $CLOC_VERSION"
BUILD_ARGS+=" --build-arg CLOC_VERSION=$CLOC_VERSION"

if [[ -z "$SCC_VERSION" ]] ; then
  # source defaults
  source ./env
fi
echo ">> Scc version: $SCC_VERSION"
BUILD_ARGS+=" --build-arg SCC_VERSION=$SCC_VERSION"

# Use Docker BuildKit
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file docker/loc-Debian.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
