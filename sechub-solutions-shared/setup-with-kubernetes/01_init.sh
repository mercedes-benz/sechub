#!/bin/bash
# SPDX-License-Identifier: MIT
set -e

cd `dirname $0`
source include.sh

cd "$SECHUB_K8S_BUILDDIR"
# Apply all .yaml files:
for i in 01_init/*.yaml ; do
  kubectl_apply "$i"
done

# Create configmaps

# Create secrets
