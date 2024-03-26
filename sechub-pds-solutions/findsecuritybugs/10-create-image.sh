#!/bin/bash
# SPDX-License-Identifier: MIT

cd `dirname $0`

REGISTRY="$1"
VERSION="$2"
BASE_IMAGE="$3"

usage() {
  cat - <<EOF
usage: $0 <docker registry> <version tag> <base image>
Builds a docker image of SecHub PDS with FindSecurityBugs
for <docker registry> with tag <version tag>.
Required: <base image> ; for example ghcr.io/mercedes-benz/sechub/pds-base

Additionally these environment variables can be defined:
- FINDSECURITYBUGS_VERSION - version of FindSecurityBugs to use. E.g. 1.12.0
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

  if [[ -z "$FINDSECURITYBUGS_SHA256SUM" ]] ; then
    echo "FATAL: Please define sha256 checksum in FINDSECURITYBUGS_SHA256SUM environment variable"
    exit 1
  fi

  echo ">> FindSecurityBugs sha256sum: $FINDSECURITYBUGS_SHA256SUM"
  BUILD_ARGS+=" --build-arg FINDSECURITYBUGS_SHA256SUM=$FINDSECURITYBUGS_SHA256SUM"
fi

echo "Copying install-java scripts into the docker directory"
cp -rf ../../sechub-solutions-shared/install-java/ docker/

export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1
docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file docker/FindSecurityBugs-Debian.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
