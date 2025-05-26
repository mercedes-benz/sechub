#!/bin/bash
# SPDX-License-Identifier: MIT
set -e

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $PDS_GITLEAKS_HELMCHART | awk -F'/' '{print $NF}')

cd "$SECHUB_K8S_BUILDDIR/20_deploy_pds-gitleaks/"
echo "### Installing pds-gitleaks image $PDS_GITLEAKS_IMAGE_REGISTRY:$PDS_GITLEAKS_IMAGE_TAG via Helm"
helm_install_or_upgrade $HELMCHART_NAME "../../$PDS_GITLEAKS_HELMCHART" values.yaml
