#!/bin/bash
set -e

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $SECHUB_SERVER_HELMCHART | awk -F'/' '{print $NF}')

cd "$SECHUB_K8S_BUILDDIR/10_deploy_sechub_server/"
if [ ! -d $HELMCHART_NAME ] ; then
  pull_and_extract_helm_chart "$SECHUB_SERVER_HELMCHART"
fi

echo "### Installing SecHub server image $SECHUB_SERVER_IMAGE_REGISTRY:$SECHUB_SERVER_IMAGE_TAG via Helm"
helm_install_or_upgrade $HELMCHART_NAME values.yaml
