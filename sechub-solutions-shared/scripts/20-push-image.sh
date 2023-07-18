#!/bin/bash
# SPDX-License-Identifier: MIT

# Generic script for pushing a container image to Docker registry

REGISTRY="$1"
VERSION="$2"
PUSH_LATEST="$3" # optional

usage() {
  MYSELF=`basename $0`
  cat - <<EOF

usage: $MYSELF <docker registry> <version tag> [yes]
Pushes a previously built image to <docker registry> with tag <version tag>.
If "yes" is provided as 3rd argument, then <docker registry>:latest will also be pushed.
EOF
}

FAILED=false
if [[ -z "$REGISTRY" ]]
then
  echo "Please provide the registry and image name as 1st argument."
  FAILED=true
fi

if [[ -z "$VERSION" ]]
then
  echo "Please provide the version number as 2nd argument."
  FAILED=true
fi

if $FAILED ; then
  usage
  exit 1
fi

echo "*****************************"
echo " Publish Docker Image "
echo "*****************************"
echo "Pushing docker image: $REGISTRY:$VERSION"
docker push "$REGISTRY:$VERSION"

if [[ "$PUSH_LATEST" == "yes" ]]
then
    echo "Pushing docker image: $REGISTRY:latest"
    docker push "$REGISTRY:latest"
fi
