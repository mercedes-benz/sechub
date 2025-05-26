#!/bin/bash
set -e

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $PDS_PMD_HELMCHART | awk -F'/' '{print $NF}')

cd "$SECHUB_K8S_BUILDDIR/20_deploy_pds-pmd/"
echo "### Installing pds-pmd image $PDS_PMD_IMAGE_REGISTRY:$PDS_PMD_IMAGE_TAG via Helm"
helm_install_or_upgrade $HELMCHART_NAME "../../$PDS_PMD_HELMCHART" values.yaml
