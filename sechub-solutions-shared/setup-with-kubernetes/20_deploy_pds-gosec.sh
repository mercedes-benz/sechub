#!/bin/bash
# SPDX-License-Identifier: MIT
set -e

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $PDS_GOSEC_HELMCHART | awk -F'/' '{print $NF}')

cd "$SECHUB_K8S_BUILDDIR/20_deploy_pds-gosec/"
echo "### Installing pds-gosec image $PDS_GOSEC_IMAGE_REGISTRY:$PDS_GOSEC_IMAGE_TAG via Helm"
helm_install_or_upgrade $HELMCHART_NAME "../../$PDS_GOSEC_HELMCHART" values.yaml
