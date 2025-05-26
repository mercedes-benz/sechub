#!/bin/bash
# SPDX-License-Identifier: MIT

source ./../common-containerscript.sh

function usage() {
    echo "Usage: $script_name <port>"
}

# setting default values for keycloak admin user and password
export KEYCLOAK_ADMIN=${KEYCLOAK_ADMIN:-admin}
export KEYCLOAK_ADMIN_PASSWORD=${KEYCLOAK_ADMIN_PASSWORD:-admin}

addEnv "DATABASE_START_MODE=server"
addEnv "KEYCLOAK_ADMIN=$KEYCLOAK_ADMIN"
addEnv "KEYCLOAK_ADMIN_PASSWORD=$KEYCLOAK_ADMIN_PASSWORD"

defineContainerPort 8080
if [[ -z "$1" ]]; then
    echo "No port given, using default port 8080"
    defineExposedPort 8080
    addEnv "CONTAINER_PORT=8080"
else
    defineExposedPort $1
    addEnv "CONTAINER_PORT=$1"
fi

defineImage "keycloak"
defineContainerName "keycloak"
ensureImageBuild
ensureContainerNotRunning

startContainer
