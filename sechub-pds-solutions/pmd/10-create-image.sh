#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

REGISTRY="$1"
VERSION="$2"
BASE_IMAGE="$3"  # optional
DEFAULT_BASE_IMAGE="ubuntu:20.04"

usage() {
  cat - <<EOF
usage: $0 <docker registry> <version tag> [<base image>]
Builds a docker image of SecHub PDS with PMD
for <docker registry> with tag <version tag>.
Optional: <base image> ; defaults to $DEFAULT_BASE_IMAGE

Additionally these environment variables can be defined:
- PDS_VERSION - version of SecHub PDS to use. E.g. 0.27.0
- PMD_VERSION - PMD version to use. E.g. 6.46.0
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
    BASE_IMAGE="$DEFAULT_BASE_IMAGE"
fi

BUILD_ARGS="--build-arg BASE_IMAGE=$BASE_IMAGE"
echo ">> Base image: $BASE_IMAGE"

if [[ ! -z "$PDS_VERSION" ]] ; then
    echo ">> SecHub PDS version: $PDS_VERSION"
    BUILD_ARGS+=" --build-arg PDS_VERSION=$PDS_VERSION"
fi

if [[ ! -z "$PMD_VERSION" ]] ; then
    echo ">> PMD version: $PMD_VERSION"
    BUILD_ARGS+=" --build-arg PMD_VERSION=$PMD_VERSION"
fi

docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file docker/PMD-Ubuntu.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
