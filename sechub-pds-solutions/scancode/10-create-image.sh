#!/bin/bash
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
Required: <base image> ; for example ghcr.io/mercedes-benz/sechub/pds-base

These environment variables can be defined:
Optional: SCANCODE_VERSION - Scancode version to use. E.g. 31.2.4. The version number can be found at: https://pypi.org/project/scancode-toolkit/.
Optional: SPDX_TOOL_VERSION - SPDX tool version. E.g. 1.1.5. Releases can be found at: https://mvnrepository.com/artifact/org.spdx/tools-java.
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

if [[ -z "$BASE_IMAGE" ]] ; then
  echo "Please provide a base image as 3rd parameter."
  FAILED=true
fi

if $FAILED ; then
  usage
  exit 1
fi

BUILD_ARGS="--build-arg BASE_IMAGE=$BASE_IMAGE"
echo ">> Base image: $BASE_IMAGE"

if [[ -z "$SCANCODE_VERSION" ]] ; then
  # source defaults
  source ./env
fi
BUILD_ARGS+=" --build-arg SCANCODE_VERSION=$SCANCODE_VERSION"
echo ">> Scancode version: $SCANCODE_VERSION"

if [[ -z "$SPDX_TOOL_VERSION" ]] ; then
  # source defaults
  source ./env
fi
BUILD_ARGS+=" --build-arg SPDX_TOOL_VERSION=$SPDX_TOOL_VERSION"
echo ">> SPDX Tool version: $SPDX_TOOL_VERSION"

# Use Docker BuildKit
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file docker/Scancode-Debian.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
