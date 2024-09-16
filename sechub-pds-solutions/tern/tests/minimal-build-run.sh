#!/bin/bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")
source build-run.sh

name="sechub-test-minimal"
dockerfile="minimal/Minimal.dockerfile"

build_and_run "$name" "$dockerfile" 