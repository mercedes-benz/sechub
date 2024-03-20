#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

set -e
SCRIPT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )" # absolute directory of this script

cd $(dirname "$0")
SECHUB_SERVER_PORT=$1
PDS_PORT=$2 # currently not really used, reserved for later access

echo "[START] setup integration test scenario for github action 'scan' "
echo "        precondition: SecHub server mut be started locally"

echo "> Handle settings"
if [[ "$SECHUB_SERVER_PORT" == "" ]]; then
   SECHUB_SERVER_PORT=8443
   echo "SECHUB_SERVER_PORT was not defined - use fallback: $SECHUB_SERVER_PORT"
fi
if [[ "$PDS_PORT" == "" ]]; then
   PDS_PORT=8444
   echo "PDS_PORT was not defined - use fallback: $PDS_PORT"
fi
if [[ "$SECHUB_SERVER" == "" ]]; then
    SECHUB_SERVER="https://localhost:$SECHUB_SERVER_PORT"
   echo "SECHUB_SERVER was not defined - use fallback: $SECHUB_SERVER"
fi
if [[ "$SECHUB_USERID" == "" ]]; then
   SECHUB_USERID="int-test_superadmin"
   echo "SECHUB_USERID was not defined - use fallback: $SECHUB_USERID"
fi
if [[ "$SECHUB_APITOKEN" == "" ]]; then
   SECHUB_APITOKEN="int-test_superadmin-pwd"
   echo "SECHUB_APITOKEN was not defined - use fallback: $SECHUB_APITOKEN"
fi

## export for calls: SECHUB_SERVER, SECHUB_USERID and SECHUB_APITOKEN as environment variables
export SECHUB_SERVER
export SECHUB_USERID
export SECHUB_APITOKEN

# we use the given sechub user (integration test admin) as user itself
# means we need no user creation,signup etc.
user=$SECHUB_USERID

function createData(){
   number=$1
   type=$2
   trafficLight=$3

   project="test-project-$number"
   profile="test-profile-$number"
   executor="executor-$type-$trafficLight"
   
   echo "> Create test project:$project for user:$user"
   ./sechub-api.sh project_create $project $user "Testproject $number for integration tests"
   ./sechub-api.sh project_assign_user $project $user # assign user to project

   echo "> Create executor config: '${executor}'"
   ./sechub-api.sh executor_create "$SCRIPT_DIR/test-config/${executor}.json"

   echo "> Create profile: '$profile'"
   ./sechub-api.sh profile_create $profile $executor

   echo "> Assign profile: '$profile' to project: '$project'"
   ./sechub-api.sh project_assign_profile $project $profile
}

echo "> Prepare sechub api script usage"
cd ../../../..
# at root level now

cd sechub-developertools/scripts
pwd

echo "> Check server alive"
./sechub-api.sh alive_check

createData 1 codescan green 
createData 2 codescan yellow
createData 3 codescan red
createData 4 webscan red
createData 5 secretscan yellow
createData 6 licensescan green

./sechub-api.sh project_set_whitelist_uris test-project-4 https://vulnerable.demo.example.com
