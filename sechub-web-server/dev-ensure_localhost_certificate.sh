#!/bin/bash
# SPDX-License-Identifier: MIT

source dev-base.sh

if [ -f "$DEV_CERT_FILE" ]; then
    exit 0
fi
createLocalhostCertifcate