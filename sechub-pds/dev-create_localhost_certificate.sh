#!/bin/bash
# SPDX-License-Identifier: MIT

source dev-base.sh

if [ -f "$DEV_CERT_FILE" ]; then
    echo "Remove old localhost certificate"
    rm "$DEV_CERT_FILE"
fi
createLocalhostCertifcate
