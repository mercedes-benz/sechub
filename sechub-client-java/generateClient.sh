#!/bin/bash 

# SPDX-License-Identifier: MIT

genFolder="$1"

chmod +x "$genFolder/gradlew"
"$genFolder/gradlew" --project-dir "$genFolder" build