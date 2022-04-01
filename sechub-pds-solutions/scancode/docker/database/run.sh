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
    pg_ctlcluster 12 main start

cat - <<EOF | psql
CREATE USER pds;
CREATE DATABASE pds OWNER pds;
GRANT ALL ON DATABASE pds TO pds;
CREATE USER $DATABASE_USERNAME WITH PASSWORD '$DATABASE_PASSWORD';
EOF

cat - <<EOF | psql --username pds pds
CREATE SCHEMA IF NOT EXISTS $DATABASE_USERNAME AUTHORIZATION pds;
GRANT ALL ON SCHEMA $DATABASE_USERNAME TO $DATABASE_USERNAME;
EOF

    # Create a new user pds
    #psql --command="CREATE USER pds;"

    # Create a new database pds which belongs to the user pds
    #psql --command="CREATE DATABASE pds OWNER pds;"

    # Grant all privileges to user pds on database pds
    #psql --command="GRANT ALL ON DATABASE pds TO pds;"

    # As pds user: create a new schema and authorize pds to the schema
    #psql --username pds --command="CREATE SCHEMA IF NOT EXISTS $DATABASE_USERNAME AUTHORIZATION pds;" pds

    # Create a new user with a password
    #psql --command="CREATE USER $DATABASE_USERNAME WITH PASSWORD '$DATABASE_PASSWORD';"

    # As pds user: give all privileges of the schema to the new user
    #psql --username pds --command="GRANT ALL ON SCHEMA $DATABASE_USERNAME TO $DATABASE_USERNAME;"

    # check PostgreSQL server status,
    # for the container to stay alive
    while pg_ctlcluster 12 main status
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