#!/bin/bash
# SPDX-License-Identifier: MIT

source ./../common-containerscript.sh

function usage() {
    echo "Usage: $script_name <port>" 
}

if [[ -z "$1" ]]; then
    echo "No port given, using default port 8080"
    defineExposedPort 8080
else
    defineExposedPort $1
fi
defineImage "keycloak"
defineContainerName "keycloak"

killContainer
