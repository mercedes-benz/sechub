#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

cd `dirname $0`

REGISTRY="$1"
VERSION="$2"
BASE_IMAGE="$3"

usage() {
  cat - <<EOF

usage: $0 <docker registry> <version tag> <base image>
Builds a docker image of SecHub PDS with Scancode
for <docker registry> with tag <version tag>.
Required: <base image> ; for example ghcr.io/mercedes-benz/sechub/pds-base:v0.32.1

Additionally these environment variables can be defined:
- SCANCODE_VERSION - Scancode version to use. E.g. 31.2.4
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

if [[ ! -z "$SCANCODE_VERSION" ]] ; then
    echo ">> Scancode version: $SCANCODE_VERSION"
    BUILD_ARGS+=" --build-arg SCANCODE_VERSION=$SCANCODE_VERSION"
fi

if [[ ! -z "$SPDX_TOOL_VERSION" ]] ; then
    echo ">> SPDX Tool version: $SPDX_TOOL_VERSION"
    BUILD_ARGS+=" --build-arg SPDX_TOOL_VERSION=$SPDX_TOOL_VERSION"
fi

if [[ ! -z "$SPDX_TOOL_CHECKSUM" ]] ; then
    echo ">> SPDX Tool checksum: $SPDX_TOOL_CHECKSUM"
    BUILD_ARGS+=" --build-arg SPDX_TOOL_CHECKSUM=$SPDX_TOOL_CHECKSUM"
fi

docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file docker/Scancode-Debian.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
