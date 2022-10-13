#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

REGISTRY="$1"
VERSION="$2"
BASE_IMAGE="$3"

usage() {
  cat - <<EOF
usage: $0 <docker registry> <version tag> <base image>
Builds a docker image of SecHub PDS with Checkmarx Wrapper
for <docker registry> with tag <version tag>.
Required: <base image> ; for example ghcr.io/mercedes-benz/sechub/pds-base:v0.32.1

Additionally these environment variables can be defined:
- BUILD_TYPE - The build type of the Checkmarx-Wrapper. For more information see `env` file.
- CHECKMARX_WRAPPER_VERSION - Checkmarx Wrapper version to use. E.g. 0.32.1
EOF
}

if [[ -z "$REGISTRY" ]] ; then
  echo "Please provide a docker registry server as 1st parameter."
  exit 1
fi

if [[ -z "$VERSION" ]] ; then
  echo "Please provide a version for the container as 2nd parameter."
  exit 1
fi

if [[ -z "$BASE_IMAGE" ]]; then
  echo "Please provide a base image as 3rd parameter."
  exit 1
fi

BUILD_ARGS="--build-arg BASE_IMAGE=$BASE_IMAGE"
echo ">> Base image: $BASE_IMAGE"

if [[ ! -z "$BUILD_TYPE" ]] ; then
    BUILD_ARGS+=" --build-arg BUILD_TYPE=$BUILD_TYPE"
    echo ">> Build type: $BUILD_TYPE"
fi

if [[ ! -z "$CHECKMARX_WRAPPER_VERSION" ]] ; then
    BUILD_ARGS+=" --build-arg CHECKMARX_WRAPPER_VERSION=$CHECKMARX_WRAPPER_VERSION"
    echo ">> Checkmarx Wrapper version: $CHECKMARX_WRAPPER_VERSION"
fi

# Use Docker BuildKit
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file docker/Checkmarx-Debian.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
