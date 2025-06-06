#!/bin/bash 
# SPDX-License-Identifier: MIT

source ./../common-containerscript.sh

function usage() {
    echo "Usage: $script_name <port>" 
}
default_port=49152

assertNotEmpty "POSTGRES_DB_USER missing" $POSTGRES_DB_USER
assertNotEmpty "POSTGRES_DB_PASSWORD missing" $POSTGRES_DB_PASSWORD
assertNotEmpty "POSTGRES_DB_NAME missing" $POSTGRES_DB_NAME

addEnv "DATABASE_START_MODE=server"
addEnv "POSTGRES_DB_USER=$POSTGRES_DB_USER"
addEnv "POSTGRES_DB_PASSWORD=$POSTGRES_DB_PASSWORD"
addEnv "POSTGRES_DB_NAME=$POSTGRES_DB_NAME"

defineContainerPort 5432
if [[ -z "$1" ]]; then
    echo "No port given, using default port 49152"
    defineExposedPort $default_port
else
  defineExposedPort $1
fi
defineImage "sechub-test-postgres"

ensureImageBuild


ensureContainerNotRunning

startContainer
