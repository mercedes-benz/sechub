#!/usr/bin/bash
# SPDX-License-Identifier: MIT

cd $(dirname "$0")
source build-run.sh

name="sechub-test-debian"
dockerfile="debian/Debian.dockerfile"

build_and_run "$name" "$dockerfile" 