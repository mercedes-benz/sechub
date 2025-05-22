#!/bin/bash
set -e

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $PDS_PREPARE_HELMCHART | awk -F'/' '{print $NF}')

cd "$SECHUB_K8S_BUILDDIR/20_deploy_pds-prepare/"
echo "### Installing pds-prepare image $PDS_PREPARE_IMAGE_REGISTRY:$PDS_PREPARE_IMAGE_TAG via Helm"
helm_install_or_upgrade $HELMCHART_NAME "../../$PDS_PREPARE_HELMCHART" values.yaml
