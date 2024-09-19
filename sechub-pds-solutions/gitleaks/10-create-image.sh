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
Builds a docker image of SecHub PDS with GoSec
for <docker registry> with tag <version tag>.
Required: <base image> ; for example ghcr.io/mercedes-benz/sechub/pds-base

Additionally these environment variables can be defined:
- BUILD_TYPE - How to get the Secret-Validation Wrapper jar. Defaults to "$DEFAULT_BUILD_TYPE"
- BUILDER_BASE_IMAGE - Base image for the build containers (see dockerfile)
- GITLEAKS_VERSION - Gitleaks version to use. E.g. 8.15.3
- SECRETVALIDATION_WRAPPER_VERSION - Secret-Validation Wrapper version to use. Example: 1.0.0
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

if [[ ! -z "$BUILD_TYPE" ]] ; then
  BUILD_ARGS+=" --build-arg BUILD_TYPE=$BUILD_TYPE"
  echo ">> Build type: $BUILD_TYPE"
fi

if [[ -z "$GITLEAKS_VERSION" ]] ; then
  # source defaults
  source ./env
fi
echo ">> Gitleaks version: $GITLEAKS_VERSION"
BUILD_ARGS+=" --build-arg GITLEAKS_VERSION=$GITLEAKS_VERSION"

echo ">> Secret-Validation Wrapper version: $SECRETVALIDATION_WRAPPER_VERSION"
BUILD_ARGS+=" --build-arg SECRETVALIDATION_WRAPPER_VERSION=$SECRETVALIDATION_WRAPPER_VERSION"

# Use Docker BuildKit
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

echo "docker build --pull --no-cache $BUILD_ARGS --tag "$REGISTRY:$VERSION" --file docker/Gitleaks.dockerfile docker/"
docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file docker/Gitleaks.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
