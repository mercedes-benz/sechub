#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

name="sechub-test-minimal"

docker rm "$name"
docker build --tag "$name" --file minimal/Minimal.dockerfile .
docker run --tty --interactive --name "$name" "$name" 