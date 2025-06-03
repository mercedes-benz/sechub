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
    echo "Starting Keycloak server on port ${KEYCLOAK_CONTAINER_PORT}..."
    exec /opt/keycloak/bin/kc.sh start-dev --import-realm --http-port="$KEYCLOAK_CONTAINER_PORT" &

    # Wait for the server to start
    sleep 30

    # Create user
    create_user

    # Keep the container running otherwise it will exit because the server is running in the background
    # this is necessary to create an initial keycloak user
    tail -f /dev/null
}

create_user () {
    echo "Creating user ${KEYCLOAK_INITIAL_USER} with password ${KEYCLOAK_INITIAL_USER} in realm 'web-ui-server-local'..."

    # Login to Keycloak
    /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:"${KEYCLOAK_CONTAINER_PORT}" --realm master --user "${KEYCLOAK_ADMIN}" --password "${KEYCLOAK_ADMIN_PASSWORD}"

    # Create a new user
    /opt/keycloak/bin/kcadm.sh create users -r web-ui-server-local -s username="${KEYCLOAK_INITIAL_USER}" -s enabled=true -s email=int-test_superadmin@sechub.example.org

    # Set password for the new user
    /opt/keycloak/bin/kcadm.sh set-password -r web-ui-server-local --username int-test_superadmin --new-password "${KEYCLOAK_INITIAL_USER_PASSWORD}"
}

if [ "$KEYCLOAK_START_MODE" = "server" ]
then
    server
else
    debug
fi
