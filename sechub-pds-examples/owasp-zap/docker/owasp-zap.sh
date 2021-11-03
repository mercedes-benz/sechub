#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

echo "###########################"
echo "# Starting OWASP ZAP scan #"
echo "###########################"
echo ""
echo "Target URL: $WEBSCAN_TARGETURL"

java -jar $TOOL_FOLDER/owaspzap-wrapper.jar --targetURL "$WEBSCAN_TARGETURL"
cp /workspace/owaspzap-report.json "$PDS_JOB_RESULT_FILE"