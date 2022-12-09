#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

REGISTRY="$1"
VERSION="$2"
BASE_IMAGE="$3"

usage() {
  cat - <<EOF
usage: $0 <docker registry> <version tag> [<base image>]
Builds a docker image of SecHub PDS with OWASP ZAP
for <docker registry> with tag <version tag>.
Required: <base image> ; for example ghcr.io/mercedes-benz/sechub/pds-base:v0.32.1

Additionally these environment variables can be defined:
- OWASPZAP_VERSION - OWASP ZAP version to use. E.g. 2.11.1
EOF
}

if [[ -z "$REGISTRY" ]] ; then
  echo "Please provide a docker registry server as 1st parameter."
  exit 1
fi

if [[ -z "$VERSION" ]] ; then
  echo "Please provide a version for the container as 2nd parameter."
  exit 1
fi
echo ">> Building: $REGISTRY:$VERSION"

BUILD_ARGS="--build-arg BASE_IMAGE=$BASE_IMAGE"
echo ">> Base image: $BASE_IMAGE"

if [[ ! -z "$PDS_VERSION" ]] ; then
    echo ">> SecHub PDS version: $PDS_VERSION"
    BUILD_ARGS+=" --build-arg PDS_VERSION=$PDS_VERSION"
fi

# Enforce OWASPZAP_SHA256SUM is defined when building custom version of find-sec-bugs
if [[ ! -z "$OWASPZAP_VERSION" ]] ; then
  echo ">> OWASP-ZAP version: $OWASPZAP_VERSION"
  BUILD_ARGS+=" --build-arg OWASPZAP_VERSION=$OWASPZAP_VERSION"

  if [[ -z "$OWASPZAP_SHA256SUM" ]] ; then
    echo "FATAL: Please define sha256 checksum in OWASPZAP_SHA256SUM environment variable"
    exit 1
  fi

  echo ">> OWASP-ZAP sha256sum: $OWASPZAP_SHA256SUM"
  BUILD_ARGS+=" --build-arg OWASPZAP_SHA256SUM=$OWASPZAP_SHA256SUM"
fi

if [[ ! -z "$OWASPZAP_WRAPPER_VERSION" ]] ; then
    echo ">> Owasp Zap Wrapper version: $OWASPZAP_WRAPPER_VERSION"
    BUILD_ARGS+=" --build-arg OWASPZAP_WRAPPER_VERSION=$OWASPZAP_WRAPPER_VERSION"
fi

docker build --pull --no-cache $BUILD_ARGS \
       --tag "$REGISTRY:$VERSION" \
       --file docker/Owasp-Zap-Debian.dockerfile docker/
docker tag "$REGISTRY:$VERSION" "$REGISTRY:latest"
