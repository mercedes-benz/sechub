#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

REGISTRY="$1"
VERSION="$2"
BASE_IMAGE="$3"  # optional
BUILD_TYPE="$4" # optional
DEFAULT_BASE_IMAGE="debian:11-slim"
DEFAULT_BUILD_TYPE="download"

usage() {
  cat - <<EOF
usage: $0 <docker registry> <version tag> [<base image>]
Builds a docker image of SecHub PDS for <docker registry> 
with tag <version tag>.

Optional: <base image> ; defaults to $DEFAULT_BASE_IMAGE
Optional: <build type> ; defaults to $DEFAULT_BUILD_TYPE

Additionally these environment variables can be defined:
- PDS_VERSION - version of SecHub PDS to use. E.g. 0.32.0
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

if [[ -z "$BUILD_TYPE" ]]; then
    BUILD_TYPE="$DEFAULT_BUILD_TYPE"
fi

if [[ -z "$BASE_IMAGE" ]]; then
    BASE_IMAGE="$DEFAULT_BASE_IMAGE"
fi

BUILD_ARGS="--build-arg BASE_IMAGE=$BASE_IMAGE"
echo ">> Base image: $BASE_IMAGE"

BUILD_ARGS+=" --build-arg BUILD_TYPE=$BUILD_TYPE"
echo ">> Build type: $BUILD_TYPE"

if [[ ! -z "$PDS_VERSION" ]] ; then
    echo ">> SecHub PDS version: $PDS_VERSION"
    BUILD_ARGS+=" --build-arg PDS_VERSION=$PDS_VERSION"
fi

# Use Docker BuildKit
# nesessary for switching between build stages
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file docker/PDS-Debian.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
