# This file is meant to be included by the scripts in this directoy

## Vars
SECHUB_K8S_BUILDDIR="build"
SECHUB_K8S_TEMPLATEDIR="templates"

# Default values:
KUBECONFIG_DEFAULT="$HOME/.kube/config"
SECHUB_INITIALADMIN_APITOKEN_DEFAULT="demo"
SECHUB_NAMESPACE_DEFAULT="sechub-testing"
SECHUB_SERVER_IMAGE_REGISTRY_DEFAULT="ghcr.io/mercedes-benz/sechub/sechub-server"
SECHUB_SERVER_IMAGE_TAG_DEFAULT="latest"
SECHUB_SERVER_HELMCHART_DEFAULT="../../sechub-solution/helm/sechub-server"

MANDATORY_EXECUTABLES="helm kubectl"  # Space separated list

# Environment variable names that will used to replace {{ .MYVAR }} in the template files.
TEMPLATE_VARIABLES=" \
  SECHUB_INITIALADMIN_APITOKEN \
  SECHUB_NAMESPACE \
  SECHUB_SERVER_HELMCHART \
  SECHUB_SERVER_IMAGE_REGISTRY \
  SECHUB_SERVER_IMAGE_TAG \
"

## Functions
function check_executable_is_installed(){
  executable="$1"
  exe_path=`which $executable`
  if [ ! -x "$exe_path" ] ; then
    echo "FATAL: Mandatory executable \"$executable\" not found in PATH. Exiting..."
    exit 1
  fi
}

# Return value of environment variable $1. If empty the return ${1}_DEFAULT
#   Example: If $SECHUB_NAMESPACE is empty then $SECHUB_NAMESPACE_DEFAULT is returned
#            else $SECHUB_NAMESPACE is returned
function env_var_or_default {
  local var="$1"
  local var_default="${1}_DEFAULT"

  if [ -n "${!var}" ] ; then
    echo "${!var}"
  else
    # The default variable must not be empty!
    if [ -z "${!var_default}" ] ; then
      echo "FATAL - env_var_or_default: variable $var_default is undefined!" >&2
      exit 1
    fi
    echo "${!var_default}"
  fi
}

# helm_install_or_upgrade <deployment name> [<values yaml file>]
function helm_install_or_upgrade {
  local deployment_name=$1
  local helm_dir="$2"
  local values_file=$3
  local action
  local helm_values

  if [ -n "$values_file" ] ; then
    if [ -r "$values_file" ] ; then
      helm_values="-f $values_file"
    else
      echo "FATAL - Helm deployment of \"$deployment_name\" failed: values file \"$values_file\" not found or not readable. Exiting..."
      exit 1
    fi
  fi

  local helm_status=$(helm $HELM_FLAGS status $deployment_name 2>&1)
  if [[ "$helm_status" =~ ^Error:.+not.found$ ]] ; then
    action=install
  else
    action=upgrade
  fi

  echo ">> helm $HELM_FLAGS $action $deployment_name $helm_dir/ $helm_values"
  helm $HELM_FLAGS $action $deployment_name $helm_dir/ $helm_values
  if [ $? -ne 0 ] ; then
    echo "Helm run  f a i l e d .  Please check."
    exit 1
  fi

  echo ""
  echo "# Waiting for \"$deployment_name\" Pod(s) to complete startup ..."
  kubectl $KUBE_FLAGS rollout status deployment/$deployment_name
  if [ $? -ne 0 ] ; then
    echo "Helm deployment  f a i l e d .  Please check."
    exit 1
  fi
}

function kubectl_apply {
  echo "### Applying $1"
  kubectl $KUBE_FLAGS apply -f "$1"
}

function pull_and_extract_helm_chart {
  local chart_uri="$1"
  local deployment_name=$(echo $1 | awk -F'/' '{print $NF}')

  # Cleanup
  rm -rf "$deployment_name" "$deployment_name"-*.tgz

  echo "### Pulling Helm chart \"$chart_uri\" from repository..."
  helm pull "$chart_uri"

  echo "### Extracting Helm chart \"$deployment_name\"..."
  tar xf "$deployment_name"-*.tgz
  if [ ! -d "$deployment_name" ] ; then
    echo "Helm pull failed."
    exit 1
  fi
}

## Actions

# Check prepreqs
for i in $MANDATORY_EXECUTABLES ; do
  check_executable_is_installed $i
done

# Populate env vars used in templates:
for var in $TEMPLATE_VARIABLES ; do
  export $var=$(env_var_or_default $var)
done

# Other env vars:
export KUBE_FLAGS="--namespace=$SECHUB_NAMESPACE"
export KUBECONFIG=$(env_var_or_default KUBECONFIG)
export HELM_FLAGS="--kubeconfig=$KUBECONFIG --namespace=$SECHUB_NAMESPACE"
