#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

echo "################################"
echo "# Starting OWASP ZAP mock scan #"
echo "################################"
echo ""

cp "$MOCK_FOLDER/mock.sarif.json" "$PDS_JOB_RESULT_FILE"
