#!/bin/bash
# SPDX-License-Identifier: MIT
set -e

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $WEBUI_HELMCHART | awk -F'/' '{print $NF}')

cd "$SECHUB_K8S_BUILDDIR/30_deploy_web-ui/"
echo "### Installing web-ui image $WEBUI_IMAGE via Helm"
helm_install_or_upgrade $HELMCHART_NAME "../../$WEBUI_HELMCHART" values.yaml
