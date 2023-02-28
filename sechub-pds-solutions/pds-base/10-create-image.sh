#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

REGISTRY="$1"
VERSION="$2"
PDS_VERSION="$3"
BASE_IMAGE="$4"  # optional
BUILD_TYPE="$5" # optional
DEFAULT_BASE_IMAGE="debian:11-slim"
DEFAULT_BUILD_TYPE="download"

usage() {
  cat - <<EOF
usage: $0 <docker registry> <version tag> <pds version> [<base image> [<build type>]]
Builds a docker image of SecHub PDS <pds version> for <docker registry>
with tag <version tag>.

Optional: <base image> ; defaults to $DEFAULT_BASE_IMAGE
Optional: <build type> ; defaults to $DEFAULT_BUILD_TYPE
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

if [[ -z "$PDS_VERSION" ]] ; then
  echo "Please provide a PDS release version as 3rd parameter."
  FAILED=true
fi

if $FAILED ; then
  usage
  exit 1
fi

if [[ -z "$BASE_IMAGE" ]]; then
    BASE_IMAGE="$DEFAULT_BASE_IMAGE"
fi

if [[ -z "$BUILD_TYPE" ]]; then
    BUILD_TYPE="$DEFAULT_BUILD_TYPE"
fi

BUILD_ARGS="--build-arg BASE_IMAGE=$BASE_IMAGE"
echo ">> Base image: $BASE_IMAGE"

BUILD_ARGS+=" --build-arg BUILD_TYPE=$BUILD_TYPE"
echo ">> Build type: $BUILD_TYPE"

BUILD_ARGS+=" --build-arg PDS_VERSION=$PDS_VERSION"
echo ">> SecHub PDS release version: $PDS_VERSION"

# Use Docker BuildKit
# nesessary for switching between build stages
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file docker/PDS-Debian.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
