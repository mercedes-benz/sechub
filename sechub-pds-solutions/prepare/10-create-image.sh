#!/bin/bash
# SPDX-License-Identifier: MIT

cd `dirname $0`

REGISTRY="$1"
VERSION="$2"
BASE_IMAGE="$3"

DEFAULT_BUILD_TYPE=download

usage() {
  cat - <<EOF

usage: $0 <docker registry> <version tag> <base image>
Builds a docker image of SecHub PDS with Prepare Wrapper
for <docker registry> with tag <version tag>.
Required: <base image> ; for example ghcr.io/mercedes-benz/sechub/pds-base

Additionally these environment variables can be defined:
- BUILD_TYPE - The build type of the Checkmarx-Wrapper. Defaults to "$DEFAULT_BUILD_TYPE"
- BUILDER_BASE_IMAGE - Base image for the build containers (see dockerfile)
- PREPARE_WRAPPER_VERSION - Prepare wrapper version to use. E.g. 1.0.0
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

if [[ -z "$BUILD_TYPE" ]] ; then
    BUILD_TYPE="$DEFAULT_BUILD_TYPE"
fi
BUILD_ARGS+=" --build-arg BUILD_TYPE=$BUILD_TYPE"
echo ">> Build type: $BUILD_TYPE"

if [[ ! -z "$BUILDER_BASE_IMAGE" ]] ; then
    BUILD_ARGS+=" --build-arg BUILDER_BASE_IMAGE=$BUILDER_BASE_IMAGE"
    echo ">> Builder base image: $BUILDER_BASE_IMAGE"
fi

if [[ -z "$PREPARE_WRAPPER_VERSION" ]] ; then
  # source defaults
  source ./env
fi
echo ">> Prepare wrapper version: $PREPARE_WRAPPER_VERSION"
BUILD_ARGS+=" --build-arg PREPARE_WRAPPER_VERSION=$PREPARE_WRAPPER_VERSION"

# Use Docker BuildKit
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

echo "docker build --pull --no-cache $BUILD_ARGS --tag "$REGISTRY:$VERSION" --file docker/Prepare-Debian.dockerfile docker/"
docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file docker/Prepare-Debian.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
