#!/bin/bash
# SPDX-License-Identifier: MIT
set -e

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $PDS_IAC_HELMCHART | awk -F'/' '{print $NF}')

cd "$SECHUB_K8S_BUILDDIR/20_deploy_pds-iac/"
echo "### Installing pds-iac image $PDS_IAC_IMAGE_REGISTRY:$PDS_IAC_IMAGE_TAG via Helm"
helm_install_or_upgrade $HELMCHART_NAME "../../$PDS_IAC_HELMCHART" values.yaml
