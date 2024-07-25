#!/usr/bin/bash
# SPDX-License-Identifier: MIT

cd `dirname $0`

REGISTRY="$1"
VERSION="$2"
BASE_IMAGE="$3"
SECHUB_SERVER_VERSION="$4"
DEFAULT_DOCKER_BUILD_TYPE="download"

usage() {
  cat - <<EOF

usage: $0 <docker registry> <version tag> <base image> [<SecHub server release version>]
Builds a docker image of SecHub server
for <docker registry> with tag <version tag>.
Required:
- <base image> ; An Alpine based image. Example: alpine:3.17
Optional:
- <SecHub server release version> parameter (mandatory for build type "download")
  See https://github.com/mercedes-benz/sechub/releases
  Example: 0.37.0 (The server .jar will be downloaded from the release)
- DOCKER_BUILD_TYPE environment variable
  Possible values: build copy download (see SecHub-Alpine.dockerfile)
  Default: $DEFAULT_DOCKER_BUILD_TYPE
EOF
}

#################################
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

if [[ -z "$DOCKER_BUILD_TYPE" ]]; then
  export DOCKER_BUILD_TYPE="$DEFAULT_DOCKER_BUILD_TYPE"
fi

# Make sure that only allowed build types are used. (Fallback: $DEFAULT_DOCKER_BUILD_TYPE)
case "$DOCKER_BUILD_TYPE" in
  build) ;;
  copy)
    SERVER_JAR=`ls docker/copy/sechub-server-*.jar 2>/dev/null`
    if [ -z "$SERVER_JAR" ] ; then
      echo "build type \"copy\": Please provide a sechub-server jar in folder docker/copy/"
      FAILED=true
    fi
    ;;
  download)
    if [ -z "$SECHUB_SERVER_VERSION" ] ; then
      echo "Please provide a SecHub server release version as 4th parameter."
      FAILED=true
    fi
    BUILD_ARGS="--build-arg SECHUB_VERSION=$SECHUB_SERVER_VERSION"
    ;;
  *)
    echo "Unknown build type \"$DOCKER_BUILD_TYPE\". Falling back to \"$DEFAULT_DOCKER_BUILD_TYPE\"."
    export DOCKER_BUILD_TYPE="$DEFAULT_DOCKER_BUILD_TYPE"
    ;;
esac

if $FAILED ; then
  usage
  exit 1
fi

BUILD_ARGS="$BUILD_ARGS --build-arg BASE_IMAGE=$BASE_IMAGE --build-arg BUILD_TYPE=$DOCKER_BUILD_TYPE"
cat - <<EOF
Building SecHub server image
  $REGISTRY:$VERSION
  based on image: $BASE_IMAGE
  build type: $DOCKER_BUILD_TYPE
EOF
if [ -n "$SECHUB_SERVER_VERSION" ] ; then
  echo "  from released version: v${SECHUB_SERVER_VERSION}-server"
fi

echo "Copying install-java scripts into the docker directory"
cp --recursive --force ../sechub-solutions-shared/install-java/ docker/

# Docker BuildKit settings
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

SERVER_DOCKERFILE="docker/SecHub-Alpine.dockerfile"
echo "docker build --pull --no-cache $BUILD_ARGS --tag \"$REGISTRY:$VERSION\" --file \"$SERVER_DOCKERFILE\" docker/"
docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file "$SERVER_DOCKERFILE" docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
