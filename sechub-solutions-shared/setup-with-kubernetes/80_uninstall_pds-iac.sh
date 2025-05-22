#!/bin/bash
set -e

cd `dirname $0`
source include.sh

helm $HELM_FLAGS uninstall pds-iac
