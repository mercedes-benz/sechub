#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

echo "################################"
echo "# Starting OWASP ZAP mock scan #"
echo "################################"
echo ""
echo "Target URL: $WEBSCAN_TARGETURL"

if [ "$WEBSCAN_TARGETURL" == "https://juice-shop.example.org" ]
then
    echo "Juice-Shop mock"
    cp "$MOCK_FOLDER/juice-shop.json" "$PDS_JOB_RESULT_FILE"
else
    echo "[WARN] Target URL not recognized. Returning default mock result."
    cp "$MOCK_FOLDER/default.json" "$PDS_JOB_RESULT_FILE"
fi