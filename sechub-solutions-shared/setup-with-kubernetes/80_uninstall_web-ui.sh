#!/bin/bash
# SPDX-License-Identifier: MIT
set -e

cd `dirname $0`
source include.sh

helm $HELM_FLAGS uninstall web-ui
