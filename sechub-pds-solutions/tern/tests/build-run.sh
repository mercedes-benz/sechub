#!/bin/bash
# SPDX-License-Identifier: MIT

function build_and_run() {
    name="$1"
    dockerfile="$2"

    docker rm "$name"
    docker build --tag "$name" --file "$dockerfile" .
    docker run --tty --interactive --name "$name" "$name"
}