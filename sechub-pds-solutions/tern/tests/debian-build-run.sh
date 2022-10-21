#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

name="sechub-test-debian"

docker rm "$name"
docker build --tag "$name" --file debian/Debian.dockerfile .
docker run --tty --interactive --name "$name" "$name" 