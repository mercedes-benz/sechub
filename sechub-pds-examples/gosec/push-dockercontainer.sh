#!/usr/bin/env bash

if [[ -z "$1" ]]
then
  echo ""
fi

if [[ -z "$2" ]]
then
  echo ""
fi

REGISTRY="$1"
VERSION="$2"

echo "*****************************"
echo " Publish Docker Image "
echo "*****************************"
echo "Pushing docker image: $REGISTR:$VERSION"
docker push $REGISTRY:$VERSION
