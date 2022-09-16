#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

GIT_URL="$1"
BRANCH="$2"
TAG="$3"

if [ ! -z "$TAG" ]
then
    echo "Tag: $TAG"
    git clone --depth 1 --branch "$TAG" "$GIT_URL" 
elif [ ! -z "$BRANCH" ]
then
    echo "Branch: $BRANCH"
    git clone --depth 1 --branch "$BRANCH" "$GIT_URL"
else
    echo "Cloning default branch"
    git clone --depth 1 "$GIT_URL"
fi