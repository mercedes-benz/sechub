#!/bin/bash
# SPDX-License-Identifier: MIT

source ./../common-containerscript.sh

function usage() {
    echo "Usage: $script_name <port>" 
}

if [[ -z "$1" ]]; then
    echo "No port given, using default port 8080"
    defineExposedPort 8080
    export "KEYCLOAK_CONTAINER_PORT=8080"
else
    defineExposedPort $1
    export "KEYCLOAK_CONTAINER_PORT=$1"
fi

container="keycloak_${KEYCLOAK_CONTAINER_PORT}"

defineImage "keycloak"
defineContainerName "${container}"

killContainer

# When the container was started by java and stopped by this script, the container info file
# must be removed to stop the java process
echo "Removing ${container} container info file if exists..."

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
relative_build_info="../../../src/build/tmp/keycloak_container_${exposed_port}.info"

# Check if the file exists
if [ ! -f "${script_dir}/${relative_build_info}" ]; then
    echo "No ${container} container info file found, nothing to remove."
    exit 0
fi

rm -f "${script_dir}/${relative_build_info}"
echo "${container} container info file removed."