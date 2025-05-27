#!/bin/bash
# SPDX-License-Identifier: MIT

source ./../common-containerscript.sh

function usage() {
    echo "Usage: $script_name <port>"
}

# setting default values for keycloak admin user and password
export KEYCLOAK_ADMIN=${KEYCLOAK_ADMIN:-admin}
export KEYCLOAK_ADMIN_PASSWORD=${KEYCLOAK_ADMIN_PASSWORD:-admin}
export SECHUB_SECURITY_SERVER_OAUTH2_CLIENT_SECRET=${SECHUB_SECURITY_SERVER_OAUTH2_CLIENT_SECRET:-$(uuidgen)}

echo  "${KEYCLOAK_ADMIN}:${KEYCLOAK_ADMIN_PASSWORD}"
addEnv "DATABASE_START_MODE=server"
addEnv "KEYCLOAK_ADMIN=$KEYCLOAK_ADMIN"
addEnv "KEYCLOAK_ADMIN_PASSWORD=$KEYCLOAK_ADMIN_PASSWORD"
addEnv "SECHUB_SECURITY_SERVER_OAUTH2_CLIENT_SECRET=$SECHUB_SECURITY_SERVER_OAUTH2_CLIENT_SECRET"

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

# Copy keycloak properties as local sechub-server properties using envsubst
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
local_template="${script_dir}/application-local-test-keycloak-template.yaml"
sechub_properties_local_keycloak="${script_dir}/../../../../sechub-server/src/main/resources/application-local-test-keycloak-gen.${USER}.yaml"

if [ -f "${sechub_properties_local_keycloak}" ]; then
    echo "Removing existing local Keycloak properties file: ${sechub_properties_local_keycloak}"
    rm -f "${sechub_properties_local_keycloak}"
fi

envsubst < "${local_template}" > "${sechub_properties_local_keycloak}"
