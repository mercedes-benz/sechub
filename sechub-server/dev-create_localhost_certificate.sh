#!/bin/bash

source dev-base.sh

if [ -f "$DEV_CERT_FILE" ]; then
    rm $DEV_CERT_FILE
fi
createLocalhostCertifcate