#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

debug () {
    while true
    do
	    echo "Press [CTRL+C] to stop.."
	    sleep 120
    done
}

# Start with localserver settings 
localserver () {
    check_setup

    java -Dspring.profiles.active=pds_localserver \
         -DsecHub.pds.admin.userid="$ADMIN_USERID" \
         -Dsechub.pds.admin.apitoken="$ADMIN_APITOKEN" \
         -DsecHub.pds.techuser.userid="$TECHUSER_USERID" \
         -Dsechub.pds.techuser.apitoken="$TECHUSER_APITOKEN" \
         -Dsechub.pds.workspace.rootfolder=/workspace \
         -Dsechub.pds.config.file=/pds/pds-config.json \
         -Dserver.port=8444 \
         -Dserver.address=0.0.0.0 \
         -jar /pds/sechub-pds-$PDS_VERSION.jar
}

check_setup () {
    check_variable "$ADMIN_USERID" "ADMIN_USERID"
    check_variable "$ADMIN_APITOKEN" "ADMIN_APITOKEN"
    check_variable "$TECHUSER_USERID" "TECHUSER_USERID"
    check_variable "$TECHUSER_APITOKEN" "TECHUSER_APITOKEN"
}

check_variable () {
    value="$1"
    name="$2"

    if [ -z "$value" ]
    then
        echo "Environment variable $name not set."
        exit 1
    fi
}

if [ "$START_MODE" = "localserver" ]
then
    localserver
else
    debug
fi
