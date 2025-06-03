#!/bin/sh
# SPDX-License-Identifier: MIT

# Fail on errors:
set -e

debug () {
    while true
    do
	    echo "Press [CTRL+C] to stop.."
	    sleep 2m
    done
}

server () {
    echo "Starting Keycloak server on port ${KEYCLOAK_CONTAINER_PORT}..."
    exec /opt/keycloak/bin/kc.sh start-dev --import-realm --http-port="$KEYCLOAK_CONTAINER_PORT" &

    # Wait for the server to start
    wait_for_keycloak_running

    # Create initial user
    create_initial_user

    # Keep the container running otherwise it will exit because the server is running in the background
    # this is necessary to create an initial keycloak user
    tail -f /dev/null
}

wait_for_keycloak_running () {
    # because the docker container has no curl and no wget we
    # use a java program to check that the keycloak server is available
	java /opt/keycloak/bin/KeycloakAvailabilityChecker.java
}

create_initial_user () {

    # Login to Keycloak
	echo "Login as administrator to keycloak"
    /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:"${KEYCLOAK_CONTAINER_PORT}" --realm master --user "${KEYCLOAK_ADMIN}" --password "${KEYCLOAK_ADMIN_PASSWORD}"

    # Create a new user
    echo "Creating initial user '${KEYCLOAK_INITIAL_USER}' with password '${KEYCLOAK_INITIAL_USER_PASSWORD}' in realm 'web-ui-server-local'..."
    /opt/keycloak/bin/kcadm.sh create users -r web-ui-server-local -s username="${KEYCLOAK_INITIAL_USER}" -s enabled=true -s email=${KEYCLOAK_INITIAL_USER}@sechub.example.org

	echo "Set new password for user ${KEYCLOAK_INITIAL_USER}"
    # Set password for the new user
    /opt/keycloak/bin/kcadm.sh set-password -r web-ui-server-local --username "${KEYCLOAK_INITIAL_USER}" --new-password "${KEYCLOAK_INITIAL_USER_PASSWORD}"
}

if [ "$KEYCLOAK_START_MODE" = "server" ]
then
    server
else
    debug
fi
