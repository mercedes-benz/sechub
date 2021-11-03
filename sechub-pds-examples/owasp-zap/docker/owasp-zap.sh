#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

echo "###########################"
echo "# Starting OWASP ZAP scan #"
echo "###########################"
echo ""
echo "Target URL: $PDS_SCAN_TARGET_URL"

java -jar $TOOL_FOLDER/owaspzap-wrapper.jar --targetURL "$PDS_SCAN_TARGET_URL" --report "$PDS_JOB_RESULT_FILE"