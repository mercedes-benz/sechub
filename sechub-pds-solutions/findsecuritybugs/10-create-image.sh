#!/bin/bash
# SPDX-License-Identifier: MIT

cd `dirname $0`

REGISTRY="$1"
VERSION="$2"
BASE_IMAGE="$3"

DEFAULT_BUILD_TYPE=build

usage() {
  cat - <<EOF
usage: $0 <docker registry> <version tag> <base image>
Builds a docker image of SecHub PDS with FindSecurityBugs
for <docker registry> with tag <version tag>.
Required: <base image> ; for example ghcr.io/mercedes-benz/sechub/pds-base

Additionally these environment variables can be defined:
- BUILD_TYPE - Can be "build" or "download" (download only until fsb 1.12.0)
- FINDSECURITYBUGS_VERSION - version of FindSecurityBugs to use. E.g. 1.13.0
- SPOTBUGS_VERSION - version of SpotBugs to use. E.g. 4.8.3
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

# Enforce FINDSECURITYBUGS_SHA256SUM is defined when building custom version of find-sec-bugs
if [[ ! -z "$FINDSECURITYBUGS_VERSION" ]] ; then
  echo ">> FindSecurityBugs version: $FINDSECURITYBUGS_VERSION"
  BUILD_ARGS+=" --build-arg FINDSECURITYBUGS_VERSION=$FINDSECURITYBUGS_VERSION"
fi

if [[ ! -z "$SPOTBUGS_VERSION" ]] ; then
  echo ">> SpotBugs version: $SPOTBUGS_VERSION"
  BUILD_ARGS+=" --build-arg SPOTBUGS_VERSION=$SPOTBUGS_VERSION"
fi

[ -z "$BUILD_TYPE" ] && BUILD_TYPE="$DEFAULT_BUILD_TYPE"
echo ">> build type: $BUILD_TYPE"
BUILD_ARGS+=" --build-arg BUILD_TYPE=$BUILD_TYPE"

echo "Copying install-java scripts into the docker directory"
cp -rf ../../sechub-solutions-shared/install-java/ docker/

export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1
docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file docker/FindSecurityBugs-Debian.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
