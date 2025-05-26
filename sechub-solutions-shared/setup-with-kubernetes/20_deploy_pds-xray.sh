#!/bin/bash
# SPDX-License-Identifier: MIT
set -e

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $PDS_XRAY_HELMCHART | awk -F'/' '{print $NF}')

cd "$SECHUB_K8S_BUILDDIR/20_deploy_pds-xray/"
echo "### Installing pds-xray image $PDS_XRAY_IMAGE_REGISTRY:$PDS_XRAY_IMAGE_TAG via Helm"
helm_install_or_upgrade $HELMCHART_NAME "../../$PDS_XRAY_HELMCHART" values.yaml
