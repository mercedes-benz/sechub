#!/bin/bash 
# SPDX-License-Identifier: MIT

genFolder="$1"
publishNecessary="$2"

if [[ -z "$genFolder" ]] ; then
  echo "Please provide then generation folder as first parameter."
  exit 1
fi

if [[ -z "$publishNecessary" ]] ; then
  echo "Please provide as second parameter information if the API shall be published (true) or not."
  exit 1
fi

# Make gradlew executable
chmod +x "$genFolder/gradlew"

# Build Java API
"$genFolder/gradlew" --project-dir "$genFolder" build


if [ "$publishNecessary" == "true" ]; then
    "$genFolder/gradlew" --project-dir "$genFolder" publish
fi