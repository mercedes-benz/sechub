#!/bin/bash
# SPDX-License-Identifier: MIT
set -e

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $PDS_MULTI_HELMCHART | awk -F'/' '{print $NF}')

cd "$SECHUB_K8S_BUILDDIR/20_deploy_pds-multi/"
echo "### Installing pds-multi image $PDS_MULTI_IMAGE_REGISTRY:$PDS_MULTI_IMAGE_TAG via Helm"
helm_install_or_upgrade $HELMCHART_NAME "../../$PDS_MULTI_HELMCHART" values.yaml
