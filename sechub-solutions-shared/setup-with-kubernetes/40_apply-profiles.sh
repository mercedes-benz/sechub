#!/bin/bash
set -e

cd `dirname $0`
source include.sh

PROFILES_DIR="profiles"
PROFILES_FILE_DEFAULT="sechub-testing.profiles"

## Functions:

function usage {
  cat - <<EOF

usage: $0 [<profiles definition file>]

Creates the SecHub profiles from file <profiles definition file>.
If no file is provided then $PROFILES_FILE_DEFAULT is taken
EOF
}

function executor_exists {
  local name="$1"
  local result=$(./sechub-api.sh executor_list | jq -r --arg executorname "$name" 'map(select(.name == $executorname))')
  if [ "$result" = "[]" ] ; then
    return 0
  else
    return 1
  fi
}

function apply_executor {
  local json_file="$PROFILES_DIR/$1"
  local name=$(cat $json_file | jq -r '.name')
  local action=""
  if executor_exists $name ; then
    echo -e "#   Creating executor $name"
    action="executor_create"
  else
    echo -e "#   Updating executor $name"
    action="executor_update $name"
  fi
  ./sechub-api.sh $action "$json_file"
}

function profile_exists {
  local name="$1"
  local result=$(./sechub-api.sh profile_list | jq -r --arg name "$name" 'map(select(.id == $name))')
  if [ "$result" = "[]" ] ; then
    return 0
  else
    return 1
  fi
}

function apply_profile {
  local profile="$1"
  local profile_definition_file="$PROFILES_DIR/$profile"
  local comment=$(cat $profile_definition_file | head -1)

  # Collect executor names
  local executors=""
  while read json_file ; do
    executors+=$(cat "$PROFILES_DIR/$json_file" | jq -r '.name')","
  done < <(cat "$PROFILES_DIR/$profile" | tail +2)
  # remove trailing ','
  executors=$(echo $executors | sed 's/,$//')

  # Create/update profile
  local action=""
  if profile_exists $profile ; then
    echo -e "# Creating profile \"$profile\" with executors \"$executors\""
    action="profile_create"
  else
    echo -e "# Updating profile \"$profile\" with executors \"$executors\""
    action="profile_update"
  fi
  ./sechub-api.sh $action $profile $executors $comment
}


#######################################################
FAILED=false

profile_definition_file="$1"
if [ -z "$profile_definition_file" ] ; then
  echo "# Using default profile definition file \"$PROFILES_FILE_DEFAULT\""
  profile_definition_file="$PROFILES_FILE_DEFAULT"
fi

set_sechub_connection || FAILED=true

verify_sechub_connection || FAILED=true

if $FAILED ; then
  usage
  exit 1
fi

TMP_EXECUTOR_LIST=$(mktemp --tmpdir sechub-XXXXXXXXXXXX.txt)
echo "# Gathering needed executors..."
while read profile ; do
  if [ ! -r "$PROFILES_DIR/$profile" ] ; then
    echo -e "Profile definition file \"$PROFILES_DIR/$profile\" not found. Please check!"
    exit 1
  fi
  # Ignore 1st line (profile comment)
  cat "$PROFILES_DIR/$profile" | tail +2 >> "$TMP_EXECUTOR_LIST"
done < "$profile_definition_file"
echo

echo "# Applying needed executors:"
while read executor_definition ; do
  echo -e "# - $executor_definition"
  apply_executor "$executor_definition"
done < <(cat "$TMP_EXECUTOR_LIST" | sort | uniq)
echo

echo -e "# Applying profiles:"
while read profile ; do
  apply_profile $profile
done < "$profile_definition_file"

rm -f "$TMP_EXECUTOR_LIST"
