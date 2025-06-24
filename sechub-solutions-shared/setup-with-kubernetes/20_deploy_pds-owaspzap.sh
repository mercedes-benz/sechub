#!/bin/bash
# SPDX-License-Identifier: MIT
set -e

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $PDS_OWASPZAP_HELMCHART | awk -F'/' '{print $NF}')

cd "$SECHUB_K8S_BUILDDIR/20_deploy_pds-owaspzap/"
echo "### Installing pds-owaspzap image $PDS_OWASPZAP_IMAGE_REGISTRY:$PDS_OWASPZAP_IMAGE_TAG via Helm"
helm_install_or_upgrade $HELMCHART_NAME "../../$PDS_OWASPZAP_HELMCHART" values.yaml
