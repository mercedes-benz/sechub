#!/bin/sh
# SPDX-License-Identifier: MIT

GIT_URL="$1"
BRANCH="$2"
TAG="$3"

if [ -z "$GIT_URL" ]
then
    echo "No Git url provided" 1>&2
    exit 1
fi

git_args=""

if [ ! -z "$TAG" ]
then
    echo "Tag: $TAG"
    git_args="--branch $TAG"
elif [ ! -z "$BRANCH" ]
then
    echo "Branch: $BRANCH"
    git_args="--branch $BRANCH"
else
    echo "Cloning default branch"
fi

git clone --depth 1 $git_args "$GIT_URL"
