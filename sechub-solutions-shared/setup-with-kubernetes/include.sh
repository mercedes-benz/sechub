# This file is meant to be included by the scripts in this directoy

## Vars
SECHUB_K8S_BUILDDIR="build"
SECHUB_K8S_TEMPLATEDIR="templates"
REPOSITORY_ROOT="../.."

# Default values
#  If you want to set own value, please set an env var without "_DEFAULT".
#  This will override the default values below
KUBECONFIG_DEFAULT="$HOME/.kube/config"
SECHUB_INITIALADMIN_APITOKEN_DEFAULT="demo" # api-token/password of user sechubadm
SECHUB_NAMESPACE_DEFAULT="sechub-testing" # Kubernetes namespace for the deployments
SECHUB_SERVER_IMAGE_REGISTRY_DEFAULT="ghcr.io/mercedes-benz/sechub/sechub-server" # Where to get the SecHub-server container image from
SECHUB_SERVER_IMAGE_TAG_DEFAULT="latest" # image tag of above
SECHUB_SERVER_HELMCHART_DEFAULT="$REPOSITORY_ROOT/sechub-solution/helm/sechub-server" # directory where the extracted SecHub-server Helm chart resides
SECHUB_SERVER_LB_PORT_DEFAULT="8443"
PDS_CHECKMARX_HELMCHART_DEFAULT="$REPOSITORY_ROOT/sechub-pds-solutions/checkmarx/helm/pds-checkmarx" # directory where the extracted pds-checkmarx Helm chart resides
PDS_CHECKMARX_IMAGE_REGISTRY_DEFAULT="ghcr.io/mercedes-benz/sechub/pds-checkmarx" # Where to get the pds-checkmarx container image from
PDS_CHECKMARX_IMAGE_TAG_DEFAULT="latest" # image tag of above
PDS_CHECKMARX_TOKEN_ADMINUSER_DEFAULT="undefined"
PDS_CHECKMARX_TOKEN_TECHUSER_DEFAULT="undefined"
PDS_FINDSECURITYBUGS_HELMCHART_DEFAULT="$REPOSITORY_ROOT/sechub-pds-solutions/findsecuritybugs/helm/pds-findsecuritybugs" # directory where the extracted pds-findsecuritybugs Helm chart resides
PDS_FINDSECURITYBUGS_IMAGE_REGISTRY_DEFAULT="ghcr.io/mercedes-benz/sechub/pds-findsecuritybugs" # Where to get the pds-findsecuritybugs container image from
PDS_FINDSECURITYBUGS_IMAGE_TAG_DEFAULT="latest" # image tag of above
PDS_FINDSECURITYBUGS_TOKEN_ADMINUSER_DEFAULT="undefined"
PDS_FINDSECURITYBUGS_TOKEN_TECHUSER_DEFAULT="undefined"
PDS_GITLEAKS_HELMCHART_DEFAULT="$REPOSITORY_ROOT/sechub-pds-solutions/gitleaks/helm/pds-gitleaks" # directory where the extracted pds-gitleaks Helm chart resides
PDS_GITLEAKS_IMAGE_REGISTRY_DEFAULT="ghcr.io/mercedes-benz/sechub/pds-gitleaks" # Where to get the pds-gitleaks container image from
PDS_GITLEAKS_IMAGE_TAG_DEFAULT="latest" # image tag of above
PDS_GITLEAKS_TOKEN_ADMINUSER_DEFAULT="undefined"
PDS_GITLEAKS_TOKEN_TECHUSER_DEFAULT="undefined"
PDS_GOSEC_IMAGE_REGISTRY_DEFAULT="ghcr.io/mercedes-benz/sechub/pds-gosec" # Where to get the pds-gosec container image from
PDS_GOSEC_IMAGE_TAG_DEFAULT="latest" # image tag of above
PDS_GOSEC_HELMCHART_DEFAULT="$REPOSITORY_ROOT/sechub-pds-solutions/gosec/helm/pds-gosec" # directory where the extracted pds-gosec Helm chart resides
PDS_GOSEC_TOKEN_ADMINUSER_DEFAULT="undefined"
PDS_GOSEC_TOKEN_TECHUSER_DEFAULT="undefined"
PDS_IAC_HELMCHART_DEFAULT="$REPOSITORY_ROOT/sechub-pds-solutions/iac/helm/pds-iac" # directory where the extracted pds-iac Helm chart resides
PDS_IAC_IMAGE_REGISTRY_DEFAULT="ghcr.io/mercedes-benz/sechub/pds-iac" # Where to get the pds-iac container image from
PDS_IAC_IMAGE_TAG_DEFAULT="latest" # image tag of above
PDS_IAC_TOKEN_ADMINUSER_DEFAULT="undefined"
PDS_IAC_TOKEN_TECHUSER_DEFAULT="undefined"
PDS_LOC_IMAGE_REGISTRY_DEFAULT="ghcr.io/mercedes-benz/sechub/pds-loc" # Where to get the pds-loc container image from
PDS_LOC_IMAGE_TAG_DEFAULT="latest" # image tag of above
PDS_LOC_HELMCHART_DEFAULT="$REPOSITORY_ROOT/sechub-pds-solutions/loc/helm/pds-loc" # directory where the extracted pds-loc Helm chart resides
PDS_LOC_TOKEN_ADMINUSER_DEFAULT="undefined"
PDS_LOC_TOKEN_TECHUSER_DEFAULT="undefined"

MANDATORY_EXECUTABLES="helm kubectl jq"  # Space separated list

# Environment variable names that will used to replace {{ .MYVAR }} in the template files.
TEMPLATE_VARIABLES=" \
  SECHUB_INITIALADMIN_APITOKEN \
  SECHUB_NAMESPACE \
  SECHUB_SERVER_HELMCHART \
  SECHUB_SERVER_IMAGE_REGISTRY \
  SECHUB_SERVER_IMAGE_TAG \
  SECHUB_SERVER_LB_PORT \
  PDS_CHECKMARX_HELMCHART \
  PDS_CHECKMARX_IMAGE_REGISTRY \
  PDS_CHECKMARX_IMAGE_TAG \
  PDS_CHECKMARX_TOKEN_ADMINUSER \
  PDS_CHECKMARX_TOKEN_TECHUSER \
  PDS_FINDSECURITYBUGS_HELMCHART \
  PDS_FINDSECURITYBUGS_IMAGE_REGISTRY \
  PDS_FINDSECURITYBUGS_IMAGE_TAG \
  PDS_FINDSECURITYBUGS_TOKEN_ADMINUSER \
  PDS_FINDSECURITYBUGS_TOKEN_TECHUSER \
  PDS_GITLEAKS_HELMCHART \
  PDS_GITLEAKS_IMAGE_REGISTRY \
  PDS_GITLEAKS_IMAGE_TAG \
  PDS_GITLEAKS_TOKEN_ADMINUSER \
  PDS_GITLEAKS_TOKEN_TECHUSER \
  PDS_GOSEC_HELMCHART \
  PDS_GOSEC_IMAGE_REGISTRY \
  PDS_GOSEC_IMAGE_TAG \
  PDS_GOSEC_TOKEN_ADMINUSER \
  PDS_GOSEC_TOKEN_TECHUSER \
  PDS_IAC_HELMCHART \
  PDS_IAC_IMAGE_REGISTRY \
  PDS_IAC_IMAGE_TAG \
  PDS_IAC_TOKEN_ADMINUSER \
  PDS_IAC_TOKEN_TECHUSER \
  PDS_LOC_HELMCHART \
  PDS_LOC_IMAGE_REGISTRY \
  PDS_LOC_IMAGE_TAG \
  PDS_LOC_TOKEN_ADMINUSER \
  PDS_LOC_TOKEN_TECHUSER \
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
  echo "### Waiting for \"$deployment_name\" Pod(s) to complete startup ..."
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

function set_sechub_connection {
  # SecHub server must have been deployed
  SERVER_IP=$(kubectl $KUBE_FLAGS get svc/sechub-server -o=jsonpath='{.status.loadBalancer.ingress[0].ip}')
  if [[ "$SERVER_IP" =~ (E|e)rror ]] ; then
    echo "Could not figure out the load balancer IP of SecHub server: $SERVER_IP"
    exit 1
  fi
  SERVER_PORT=$SECHUB_SERVER_LB_PORT

  export SECHUB_SERVER="https://$SERVER_IP:$SERVER_PORT"
  export SECHUB_USERID=sechubadm
  export SECHUB_APITOKEN="$SECHUB_INITIALADMIN_APITOKEN"
  # Trust all because of self-signed certificate
  export SECHUB_TRUSTALL=true
}

function symlink_sechub_api_script {
  if [ ! -L sechub-api.sh ] ; then
    echo "### Creating a symlink for sechub-api.sh script"
    rm -f sechub-api.sh
    ln -s $REPOSITORY_ROOT/sechub-developertools/scripts/sechub-api.sh .
  fi
}

function verify_sechub_connection {
  echo "# Connection test to $SECHUB_SERVER"
  SECHUB_VERSION=$(./sechub-api.sh server_version)
  if [ -z "$SECHUB_VERSION" ] || [[ "$SECHUB_VERSION" =~ Unauthorized|null ]] ; then
    echo -e "# FATAL - Connection FAILED, wrong user or no admin rights: $SECHUB_VERSION"
    return 1
  else
    echo -e "# Succeeded - SecHub server version: $SECHUB_VERSION"
  fi
}

## Actions

symlink_sechub_api_script

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
