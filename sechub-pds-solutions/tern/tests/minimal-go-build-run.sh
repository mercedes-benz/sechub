#!/usr/bin/bash
# SPDX-License-Identifier: MIT

name="sechub-test-minimal-go"
cd $(dirname "$0")
source build-run.sh

name="sechub-test-minimal-go"
dockerfile="minimal/Minimal-Go.dockerfile"

build_and_run "$name" "$dockerfile" 