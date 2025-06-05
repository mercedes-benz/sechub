#!/bin/bash
# SPDX-License-Identifier: MIT

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${script_dir}/../common-containerscript.sh"

usage() {
    echo "Usage: $0 [port]"
    echo "ENVIRONMENT VARIABLES:"
    echo "  KC_BOOTSTRAP_ADMIN_USERNAME: Keycloak admin username (default: admin)"
    echo "  KC_BOOTSTRAP_ADMIN_PASSWORD: Keycloak admin password (default: admin)"
    echo "  SECHUB_SECURITY_SERVER_OAUTH2_CLIENT_SECRET: OAuth2 client secret (default: generated)"
    echo "  KEYCLOAK_INITIAL_USER: Initial user for Keycloak (default: int-test_superadmin)"
    echo "  KEYCLOAK_INITIAL_USER_PASSWORD: Initial user password (default: int-test_superadmin-pwd)"
    echo ""
    echo "  This will generate a local Keycloak properties file for the SecHub server, which can be used with the 'local_keycloak' Spring profile."
}

default_port=8080
if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    usage
    exit 0
fi

if [[ -z "$1" ]]; then
    echo "No port given, using default port $default_port"
else
    echo "Using provided port: $1"
fi

# setting predefined or default values for keycloak admin, admin password, oauth2 client secret, initial user and password
export KC_BOOTSTRAP_ADMIN_USERNAME=${KC_BOOTSTRAP_ADMIN_USERNAME:-admin}
export KC_BOOTSTRAP_ADMIN_PASSWORD=${KC_BOOTSTRAP_ADMIN_PASSWORD:-admin}
export SECHUB_SECURITY_SERVER_OAUTH2_CLIENT_SECRET=${SECHUB_SECURITY_SERVER_OAUTH2_CLIENT_SECRET:-$(uuidgen)}

# keycloak specific variables for initial user creation, can only be set by environment variables
export KEYCLOAK_INITIAL_USER=${KEYCLOAK_INITIAL_USER:-int-test_superadmin}
export KEYCLOAK_INITIAL_USER_PASSWORD=${KEYCLOAK_INITIAL_USER_PASSWORD:-int-test_superadmin-pwd}

# set by script argument or default
export KEYCLOAK_CONTAINER_PORT="${1:-$default_port}"

# Copy keycloak properties template as local sechub-server properties using envsubst for variable substitution
# can be used with spring profile "local"
local_template="${script_dir}/application-local_keycloak_gen_template.yaml"
sechub_properties_local_keycloak="${script_dir}/../../../../sechub-server/src/main/resources/application-local_keycloak_gen.yaml"

if [ -f "${sechub_properties_local_keycloak}" ]; then
    echo "Removing existing local Keycloak properties file: ${sechub_properties_local_keycloak}"
    rm -f "${sechub_properties_local_keycloak}"
    echo "Warning: new keycloak properties file was generated for sechub server"
    echo "Warning: Please reload the application context of the sechub server to apply the changes and make sure the new keycloak properties are used."
fi

echo "Generating local Keycloak properties file from template: ${local_template} to ${sechub_properties_local_keycloak}"
envsubst < "${local_template}" > "${sechub_properties_local_keycloak}"

addEnv "KEYCLOAK_START_MODE=server"
addEnv "KC_BOOTSTRAP_ADMIN_USERNAME=$KC_BOOTSTRAP_ADMIN_USERNAME"
addEnv "KC_BOOTSTRAP_ADMIN_PASSWORD=$KC_BOOTSTRAP_ADMIN_PASSWORD"
addEnv "SECHUB_SECURITY_SERVER_OAUTH2_CLIENT_SECRET=$SECHUB_SECURITY_SERVER_OAUTH2_CLIENT_SECRET"
addEnv "KEYCLOAK_CONTAINER_PORT=$KEYCLOAK_CONTAINER_PORT"
addEnv "KEYCLOAK_INITIAL_USER=$KEYCLOAK_INITIAL_USER"
addEnv "KEYCLOAK_INITIAL_USER_PASSWORD=$KEYCLOAK_INITIAL_USER_PASSWORD"

defineContainerPort "$KEYCLOAK_CONTAINER_PORT"
defineExposedPort "$KEYCLOAK_CONTAINER_PORT"

defineImage "keycloak"
defineContainerName "keycloak_${KEYCLOAK_CONTAINER_PORT}"
ensureImageBuild
ensureContainerNotRunning
startContainer
