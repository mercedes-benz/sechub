#!/bin/bash 
# SPDX-License-Identifier: MIT

#
# This script prepares following:
# 
# zipfile_contains_inttest_codescan_with_medium.zip will be extracted to wanted target location
set -e

# Working dir: $sechubRoot/echub-systemtest/src/test/resources/fake-root/test/preparation
echo "Start copy of data.txt to $1"
cp -r "$(pwd)/inttest-data/upload/codescan-medium-findings" "$1"
