#!/bin/bash
# SPDX-License-Identifier: MIT

source ./../common-containerscript.sh

function usage() {
    echo "Usage: $script_name <port>" 
}

defineExposedPort $1
defineImage "sechub-test-postgres"

killContainer
