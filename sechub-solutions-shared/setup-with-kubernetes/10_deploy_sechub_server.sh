#!/bin/bash
set -e

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $SECHUB_SERVER_HELMCHART | awk -F'/' '{print $NF}')

cd "$SECHUB_K8S_BUILDDIR/10_deploy_sechub_server/"
echo "### Installing SecHub server image $SECHUB_SERVER_IMAGE_REGISTRY:$SECHUB_SERVER_IMAGE_TAG via Helm"
helm_install_or_upgrade $HELMCHART_NAME "../../$SECHUB_SERVER_HELMCHART" values.yaml
