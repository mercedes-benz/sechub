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

docker build -t $REGISTRY:$VERSION -f dockerfile .
docker tag $REGISTRY:$VERSION $REGISTRY:latest

