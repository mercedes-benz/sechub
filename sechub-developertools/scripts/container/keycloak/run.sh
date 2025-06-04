#!/bin/sh
# SPDX-License-Identifier: MIT

# Fail on errors:
set -e

max_attempts=60
counter=0

debug () {
    while true
    do
	    echo "Press [CTRL+C] to stop.."
	    sleep 2m
    done
}

server () {
    echo "Starting Keycloak server on port ${KEYCLOAK_CONTAINER_PORT}..."
    exec /opt/keycloak/bin/kc.sh start-dev --import-realm --http-port="$KEYCLOAK_CONTAINER_PORT" --health-enabled=true &

    # Wait for the server to start
    until wait_for_keycloak_running; do
      echo "Waiting for Keycloak to be ready... Attempt $((counter + 1)) of $max_attempts"

      counter=$((counter + 1))
      if [ $counter -ge $max_attempts ]; then
        # If the maximum number of attempts is reached, exit with an error and the container will stop
        echo "Keycloak did not start within the expected time."
        exit 1
      fi

      sleep 5  # Wait for 5 seconds before checking again
    done
    echo "Keycloak is now running on port ${KEYCLOAK_CONTAINER_PORT}."

    # Create initial user
    create_initial_user

    # Keep the container running otherwise it will exit because the server is running in the background
    # this is necessary to create an initial keycloak user
    tail -f /dev/null
}


wait_for_keycloak_running () {
    # Check if Keycloak is ready by querying the health endpoint
    curl --head -fsS "http://localhost:9000/health/ready"
}

create_initial_user () {
    # Login to Keycloak
	  echo "Login as administrator to keycloak"
    /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:"${KEYCLOAK_CONTAINER_PORT}" --realm master --user "${KC_BOOTSTRAP_ADMIN_USERNAME}" --password "${KC_BOOTSTRAP_ADMIN_PASSWORD}"

    # Create a new user
    echo "Creating initial user '${KEYCLOAK_INITIAL_USER}' with password '${KEYCLOAK_INITIAL_USER_PASSWORD}' in realm 'web-ui-server-local'..."
    /opt/keycloak/bin/kcadm.sh create users -r web-ui-server-local -s username="${KEYCLOAK_INITIAL_USER}" -s enabled=true -s email="${KEYCLOAK_INITIAL_USER}@sechub.example.org"

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
