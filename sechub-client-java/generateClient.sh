#!/bin/bash 

# SPDX-License-Identifier: MIT

genFolder="$1"

# Make gradlew executable
chmod +x "$genFolder/gradlew"

# Build Java Client
"$genFolder/gradlew" --project-dir "$genFolder" build

# Publish artifacts to local maven repository
"$genFolder/gradlew" --project-dir "$genFolder" install