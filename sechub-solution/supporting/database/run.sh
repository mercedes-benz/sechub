#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

debug () {
    while true
    do
	    echo "Press [CTRL+C] to stop.."
	    sleep 120
    done
}

server () {
    # start PostgreSQL server
    pg_ctlcluster "${POSTGRE_VERSION}" main start

    # Create a new user with a password
    psql --command="CREATE USER $DATABASE_USERNAME WITH PASSWORD '$DATABASE_PASSWORD';"

    # Create a new database sechub which belongs to the user sechub
    psql --command="CREATE DATABASE sechub OWNER $DATABASE_USERNAME;"

    # Grant all privileges to user sechub on database sechub
    psql --command="GRANT ALL ON DATABASE sechub TO $DATABASE_USERNAME;"

    # As sechub user: create a new schema and authorize sechub to the schema
    psql --username "$DATABASE_USERNAME" --command="CREATE SCHEMA IF NOT EXISTS sechub AUTHORIZATION sechub;" sechub

    # As sechub user: give all privileges of the schema to the sechub user
    psql --username sechub --command="GRANT ALL ON SCHEMA sechub TO $DATABASE_USERNAME;"

    # check PostgreSQL server status,
    # for the container to stay alive
    while pg_ctlcluster "${POSTGRE_VERSION}" main status
    do
        sleep 300
    done
}

if [ "$DATABASE_START_MODE" = "server" ]
then
    server
else
    debug
fi