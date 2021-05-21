#!/usr/bin/env bash

function debug {
    while true
    do
	    echo "Press [CTRL+C] to stop.."
	    sleep 120
    done
}

# Start with localserver settings 
function localserver {
    java -Dspring.profiles.active=pds_localserver \
         -DsecHub.pds.techuser.userid=pds-dev-techuser \
         -Dsechub.pds.techuser.apitoken="{noop}pds-dev-apitoken" \
         -DsecHub.pds.admin.userid=pds-dev-admin \
         -Dsechub.pds.admin.apitoken="{noop}pds-dev-apitoken" \
         -Dsechub.pds.workspace.rootfolder=/workspace \
         -Dsechub.pds.config.file=/pds/pds-config.json \
         -Dserver.port=8444 \
         -Dserver.address=0.0.0.0 \
         -jar /pds/sechub-pds-0.21.0.jar
}

if [[ "$START_MODE" == "localserver" ]]
then
    localserver
else
    debug
fi
