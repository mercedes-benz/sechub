#!/bin/bash
set -e

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $PDS_FINDSECURITYBUGS_HELMCHART | awk -F'/' '{print $NF}')

cd "$SECHUB_K8S_BUILDDIR/20_deploy_pds-findsecuritybugs/"
echo "### Installing pds-findsecuritybugs image $PDS_FINDSECURITYBUGS_IMAGE_REGISTRY:$PDS_FINDSECURITYBUGS_IMAGE_TAG via Helm"
helm_install_or_upgrade $HELMCHART_NAME "../../$PDS_FINDSECURITYBUGS_HELMCHART" values.yaml
