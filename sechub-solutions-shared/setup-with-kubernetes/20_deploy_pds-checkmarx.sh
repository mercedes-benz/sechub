#!/bin/bash
set -e

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $PDS_CHECKMARX_HELMCHART | awk -F'/' '{print $NF}')

cd "$SECHUB_K8S_BUILDDIR/20_deploy_pds-checkmarx/"
echo "### Installing pds-checkmarx image $PDS_CHECKMARX_IMAGE_REGISTRY:$PDS_CHECKMARX_IMAGE_TAG via Helm"
helm_install_or_upgrade $HELMCHART_NAME "../../$PDS_CHECKMARX_HELMCHART" values.yaml
