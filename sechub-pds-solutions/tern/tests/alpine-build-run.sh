#!/usr/bin/bash
# SPDX-License-Identifier: MIT

name="sechub-test-alpine"

docker rm "$name"
docker build --tag "$name" --file alpine/Alpine.dockerfile .
docker run --tty --interactive --name "$name" "$name" 