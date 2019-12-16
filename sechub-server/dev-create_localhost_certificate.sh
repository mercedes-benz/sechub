#!/bin/bash

source dev-base.sh

if [ -f "$DEV_CERT_FILE" ]; then
    echo "Remove old localhost certificate"
    rm $DEV_CERT_FILE
fi
createLocalhostCertifcate