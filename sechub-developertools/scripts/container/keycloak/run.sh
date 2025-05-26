#!/bin/sh
# SPDX-License-Identifier: MIT

debug () {
    while true
    do
	    echo "Press [CTRL+C] to stop.."
	    sleep 2m
    done
}

server () {
    echo "Starting Keycloak server on port $KEYCLOAK_PORT..."
    exec ./bin/kc.sh start-dev --import-realm --http-port="$KEYCLOAK_PORT"
}

cd /opt/keycloak

if [ ! -x ./bin/kc.sh ]; then
    echo "ERROR: /opt/keycloak/bin/kc.sh not found or not executable"
    exit 1
fi

KEYCLOAK_PORT="${CONTAINER_PORT:-8080}"

if [ "$DATABASE_START_MODE" = "server" ]
then
    server
else
    debug
fi
