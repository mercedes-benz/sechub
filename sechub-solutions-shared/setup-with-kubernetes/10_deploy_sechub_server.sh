#!/bin/bash
# SPDX-License-Identifier: MIT

cd `dirname $0`
source include.sh

HELMCHART_NAME=$(echo $SECHUB_SERVER_HELMCHART | awk -F'/' '{print $NF}')

pushd "$SECHUB_K8S_BUILDDIR/10_deploy_sechub_server/" >/dev/null 2>&1
echo "### Installing SecHub server image $SECHUB_SERVER_IMAGE_REGISTRY:$SECHUB_SERVER_IMAGE_TAG via Helm"
helm_install_or_upgrade $HELMCHART_NAME "../../$SECHUB_SERVER_HELMCHART" values.yaml

# When run for the first time
if [ "$LOADBALANCER_IP_ADDRESS" = "$LOADBALANCER_IP_ADDRESS_DEFAULT" ] ; then
  echo
  wait_for_loadbalancer_ip
  echo "### New creation of loadbalancer object. Please run these scripts again:"
  echo "./00_prepare_build.sh"
  echo "./10_deploy_sechub_server.sh"
fi
