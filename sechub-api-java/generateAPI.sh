#!/bin/bash 
# SPDX-License-Identifier: MIT

genFolder="$1"

if [[ -z "$genFolder" ]] ; then
  echo "Please provide then generation folder as first parameter."
  exit 1
fi

# Make gradlew executable
chmod +x "$genFolder/gradlew"

# Build Java API
"$genFolder/gradlew" --project-dir "$genFolder" build

# Publish artifacts to local maven repository
"$genFolder/gradlew" --project-dir "$genFolder" install