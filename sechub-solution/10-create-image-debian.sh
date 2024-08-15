#!/bin/bash
# SPDX-License-Identifier: MIT

cd `dirname $0`

REGISTRY="$1"
VERSION="$2"
BASE_IMAGE="$3"
SECHUB_SERVER_VERSION="$4"

usage() {
  cat - <<EOF

usage: $0 <docker registry> <version tag> <Debian base image> <SecHub server release version>
Builds a docker image of SecHub server
for <docker registry> with tag <version tag>.
Required:
- <base image> ; A Debian based image Example: debian:12-slim
- <SecHub server release version> ; see https://github.com/mercedes-benz/sechub/releases
  Example: 0.37.0 (The server .jar will be downloaded from the release)
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

if [[ -z "$SECHUB_SERVER_VERSION" ]]; then
  echo "Please provide a SecHub server release version as 4th parameter."
  FAILED=true
fi

if $FAILED ; then
  usage
  exit 1
fi

BUILD_ARGS="--build-arg BASE_IMAGE=$BASE_IMAGE --build-arg SECHUB_VERSION=$SECHUB_SERVER_VERSION"
cat - <<EOF
Building SecHub server image
  $REGISTRY:$VERSION
  from release version v${SECHUB_SERVER_VERSION}-server
  based on image: $BASE_IMAGE
EOF

echo "Copying install-java scripts into the docker directory"
cp --recursive --force ../sechub-solutions-shared/install-java/ docker/

# Docker BuildKit settings
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

SERVER_DOCKERFILE="docker/SecHub-Debian.dockerfile"
echo "docker build --pull --no-cache $BUILD_ARGS --tag \"$REGISTRY:$VERSION\" --file \"$SERVER_DOCKERFILE\" docker/"
docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file "$SERVER_DOCKERFILE" docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
