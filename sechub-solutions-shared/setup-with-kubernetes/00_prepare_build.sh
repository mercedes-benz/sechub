#!/bin/bash
set -e

cd `dirname $0`
source include.sh

TEMPLATE_REPLACE_SED_FILE=".template-replace.sed"

## Functions
function create_sed_replacement_command {
  local varname=$1
  if [ -z "$varname" ] ; then
    return
  fi
  local value="${!varname}"
  echo 's|{{ *\.'$varname' *}}|'$value'|g'
}

function prepare_template_replace {
  local var
  local cmd
  rm -f "$TEMPLATE_REPLACE_SED_FILE"
  for var in $TEMPLATE_VARIABLES ; do
    cmd=$(create_sed_replacement_command $var)
    echo $cmd >> "$TEMPLATE_REPLACE_SED_FILE"
  done
}

###############
# main()
rm -rf "$SECHUB_K8S_BUILDDIR"
mkdir "$SECHUB_K8S_BUILDDIR"
echo "### Preparing k8s configs from templates"
cp -r "$SECHUB_K8S_TEMPLATEDIR"/* "$SECHUB_K8S_BUILDDIR"/

# Prepare command file for sed
prepare_template_replace

cd "$SECHUB_K8S_BUILDDIR"
find . -type f | sort | while read file ; do
  echo $file
  # Replace template variables with values from env vars
  # E.g: "{{ .MYVAR }}" will be replaced with the value of $MYVAR
  sed -i -f "../$TEMPLATE_REPLACE_SED_FILE" "$file"
done
