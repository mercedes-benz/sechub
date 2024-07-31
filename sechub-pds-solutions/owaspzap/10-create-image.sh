#!/bin/bash
# SPDX-License-Identifier: MIT

REGISTRY="$1"
VERSION="$2"
BASE_IMAGE="$3"

usage() {
  cat - <<EOF

usage: $0 <docker registry> <version tag> [<base image>]
Builds a docker image of SecHub PDS with OWASP ZAP
for <docker registry> with tag <version tag>.
Required: <base image> ; Example: ghcr.io/mercedes-benz/sechub/pds-base

Additionally these environment variables can be defined:
- OWASPZAP_VERSION - OWASP ZAP version to use. Example: 2.12.0
- OWASPZAP_SHA256SUM - sha256sum of OWASP ZAP download
- OWASPZAP_WRAPPER_VERSION - Version of the SecHub PDS-OWASPZAP wrapper jar. Example: 1.0.0
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
  echo "Please provide a SecHub PDS base images as 3rd parameter."
  FAILED=true
fi

if $FAILED ; then
  usage
  exit 1
fi

echo ">> Building \"$REGISTRY:$VERSION\""

BUILD_ARGS="--build-arg BASE_IMAGE=$BASE_IMAGE"
echo ">> From base image: $BASE_IMAGE"

# Enforce OWASPZAP_SHA256SUM is defined when building custom version of find-sec-bugs
if [[ -z "$OWASPZAP_VERSION" ]] ; then
  # source defaults
  source ./env
fi
echo ">> OWASP-ZAP version: $OWASPZAP_VERSION"
BUILD_ARGS+=" --build-arg OWASPZAP_VERSION=$OWASPZAP_VERSION"

if [[ -z "$OWASPZAP_SHA256SUM" ]] ; then
  echo "FATAL: Please define sha256 checksum in OWASPZAP_SHA256SUM environment variable"
  exit 1
fi
echo ">> OWASP-ZAP sha256sum: $OWASPZAP_SHA256SUM"
BUILD_ARGS+=" --build-arg OWASPZAP_SHA256SUM=$OWASPZAP_SHA256SUM"

if [[ -z "$OWASPZAP_WRAPPER_VERSION" ]] ; then
  # source defaults
  source ./env
fi
echo ">> SecHub OWASP-ZAP Wrapper version: $OWASPZAP_WRAPPER_VERSION"
BUILD_ARGS+=" --build-arg OWASPZAP_WRAPPER_VERSION=$OWASPZAP_WRAPPER_VERSION"

# Use Docker BuildKit
export BUILDKIT_PROGRESS=plain
export DOCKER_BUILDKIT=1

docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file docker/Owasp-Zap-Debian.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
