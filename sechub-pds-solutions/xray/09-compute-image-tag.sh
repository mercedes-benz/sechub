#!/bin/bash
# SPDX-License-Identifier: MIT

# Compute image version tag for container image
# 1st argument is the pds-base version

VERSION_TAG=""
if [ -n "$1" ]; then
  VERSION_TAG="$1"
else
  # This should not happen, but in this case we just use the current date
  VERSION_TAG="`date +%Y-%m-%d`"
fi

if [[ -n "$XRAY_WRAPPER_VERSION" ]] ; then
    VERSION_TAG+="_$XRAY_WRAPPER_VERSION"
fi

echo $VERSION_TAG
