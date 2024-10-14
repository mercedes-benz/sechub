#!/bin/bash
# SPDX-License-Identifier: MIT

REGISTRY="$1"
VERSION="$2"
WEB_SERVER_VERSION="$3"
BASE_IMAGE="$4"  # optional
BUILD_TYPE="$5" # optional
DEFAULT_BASE_IMAGE="debian:12-slim"
DEFAULT_BUILD_TYPE="download"

cd `dirname $0`

usage() {
  cat - <<EOF

usage: $0 <docker registry> <version tag> <webserver version> [<base image> <build type>]

Builds a docker image of SecHub Web Server <webserver version> for <docker registry>
with tag <version tag>.

Optional environment variables or options:
BASE_IMAGE - <base image> to build from ; defaults to $DEFAULT_BASE_IMAGE
BUILD_TYPE - <build type> (one of: build copy download) ; defaults to $DEFAULT_BUILD_TYPE
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

if [[ -z "$WEB_SERVER_VERSION" ]] ; then
  echo "Please provide a SecHub Web Server release version as 3rd parameter."
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

BUILD_ARGS+=" --build-arg WEB_SERVER_VERSION=$WEB_SERVER_VERSION"
echo ">> SecHub Web Server release version: $WEB_SERVER_VERSION"

echo "Copying install-java scripts into the docker directory"
cp --recursive --force ../sechub-solutions-shared/install-java/ docker/

# Use Docker BuildKit
# nesessary for switching between build stages
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file docker/Web-Server-Debian.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
