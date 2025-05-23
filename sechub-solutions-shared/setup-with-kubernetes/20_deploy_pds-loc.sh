#!/bin/bash
set -e

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $PDS_LOC_HELMCHART | awk -F'/' '{print $NF}')

cd "$SECHUB_K8S_BUILDDIR/20_deploy_pds-loc/"
echo "### Installing pds-loc image $PDS_LOC_IMAGE_REGISTRY:$PDS_LOC_IMAGE_TAG via Helm"
helm_install_or_upgrade $HELMCHART_NAME "../../$PDS_LOC_HELMCHART" values.yaml
