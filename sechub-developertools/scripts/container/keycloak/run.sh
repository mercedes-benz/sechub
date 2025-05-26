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
    echo "Starting Keycloak server on port ${CONTAINER_PORT}..."
    exec /opt/keycloak/bin/kc.sh start-dev --import-realm --http-port="$CONTAINER_PORT" &

    # Wait for the server to start
    sleep 30

    # Create user
    create_user

    # Keep the container running otherwise it will exit because the server is running in the background
    # this is necessary to keep the create a user
    tail -f /dev/null
}

create_user () {
    echo "Creating user 'newuser' with password 'newpassword' in realm 'web-ui-server-local'..."

    # Login to Keycloak
    /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:"${CONTAINER_PORT}" --realm master --user "${KEYCLOAK_ADMIN}" --password "${KEYCLOAK_ADMIN_PASSWORD}"

    # Create a new user
    /opt/keycloak/bin/kcadm.sh create users -r web-ui-server-local -s username=int-test_superadmin -s enabled=true -s email=int-test_superadmin@sechub.example.org

    # Set password for the new user
    /opt/keycloak/bin/kcadm.sh set-password -r web-ui-server-local --username int-test_superadmin --new-password int-test_superadmin-pwd
}

if [ "$DATABASE_START_MODE" = "server" ]
then
    server
else
    debug
fi
