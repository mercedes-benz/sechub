#!/bin/bash
set -e

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $PDS_SCANCODE_HELMCHART | awk -F'/' '{print $NF}')

cd "$SECHUB_K8S_BUILDDIR/20_deploy_pds-scancode/"
echo "### Installing pds-scancode image $PDS_SCANCODE_IMAGE_REGISTRY:$PDS_SCANCODE_IMAGE_TAG via Helm"
helm_install_or_upgrade $HELMCHART_NAME "../../$PDS_SCANCODE_HELMCHART" values.yaml
