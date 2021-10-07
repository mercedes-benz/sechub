#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

echo "Target URL: $OWASPZAP_TARGETURL"
java -jar $TOOL_FOLDER/owaspzap-wrapper.jar -t "$OWASPZAP_TARGETURL"
cp /workspace/owaspzap-report.json "$PDS_JOB_RESULT_FILE"