#!/bin/bash
set -e

cd `dirname $0`
source include.sh

function usage {
  cat - <<EOF

usage: $0 <new project> <profile to assign>

Creates SecHub project <new project> and assigns scane profile <profile to assign> to it.
EOF
}

################################################
FAILED=false

project="$1"
profile="$2"

if [ -z "$project" ] ; then
  echo "Project name is missing as 1st parameter."
  FAILED=true
fi
if [ -z "$profile" ] ; then
  echo "Profile name is missing as 2nd parameter."
  FAILED=true
fi

if $FAILED ; then
  usage
  exit 1
fi

set_sechub_connection || FAILED=true
verify_sechub_connection || FAILED=true
if $FAILED ; then
  echo "Error while trying to access SecHub server. Please check above messages."
  exit 1
fi

./sechub-api.sh project_create $project sechubadm
./sechub-api.sh project_assign_profile $project $profile
