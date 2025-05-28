#!/bin/bash
# SPDX-License-Identifier: MIT

source ./../common-containerscript.sh

function usage() {
    echo "Usage: $script_name <port>"
}
default_port=8080

# setting default values for keycloak admin user and password
export KEYCLOAK_ADMIN=${KEYCLOAK_ADMIN:-admin}
export KEYCLOAK_ADMIN_PASSWORD=${KEYCLOAK_ADMIN_PASSWORD:-admin}
export SECHUB_SECURITY_SERVER_OAUTH2_CLIENT_SECRET=${SECHUB_SECURITY_SERVER_OAUTH2_CLIENT_SECRET:-$(uuidgen)}

addEnv "DATABASE_START_MODE=server"
addEnv "KEYCLOAK_ADMIN=$KEYCLOAK_ADMIN"
addEnv "KEYCLOAK_ADMIN_PASSWORD=$KEYCLOAK_ADMIN_PASSWORD"
addEnv "SECHUB_SECURITY_SERVER_OAUTH2_CLIENT_SECRET=$SECHUB_SECURITY_SERVER_OAUTH2_CLIENT_SECRET"


if [[ -z "$1" ]]; then
    echo "No port given, using default port 8080"
    defineContainerPort $default_port
    defineExposedPort $default_port
    addEnv "KEYCLOAK_CONTAINER_PORT=${default_port}"
    export "KEYCLOAK_CONTAINER_PORT=${default_port}"
else
    defineContainerPort $1
    defineExposedPort $1
    addEnv "KEYCLOAK_CONTAINER_PORT=$1"
    export "KEYCLOAK_CONTAINER_PORT=$1"
fi

defineImage "keycloak"
defineContainerName "keycloak_${KEYCLOAK_CONTAINER_PORT}"
ensureImageBuild
ensureContainerNotRunning

startContainer

# Copy keycloak properties template as local sechub-server properties using envsubst for variable substitution
# can be used with spring profile "local"
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
local_template="${script_dir}/application-local-test-keycloak-template.yaml"
sechub_properties_local_keycloak="${script_dir}/../../../../sechub-server/src/main/resources/application-local-test-keycloak-gen.${USER}.yaml"

if [ -f "${sechub_properties_local_keycloak}" ]; then
    echo "Removing existing local Keycloak properties file: ${sechub_properties_local_keycloak}"
    rm -f "${sechub_properties_local_keycloak}"
fi

envsubst < "${local_template}" > "${sechub_properties_local_keycloak}"
