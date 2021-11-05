#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

echo "###########################"
echo "# Starting OWASP ZAP scan #"
echo "###########################"
echo ""
echo "Target URL: $PDS_SCAN_TARGET_URL"
echo ""

options=""

if [ "$ZAP_ACTIVESCAN_ENABLED" = "true"  ]
then
    echo "Active scan: enabled"
    options="$options --activeScan"
else
    echo "Active scan: disabled"
fi

if [ "$ZAP_AJAXCRAWLER_ENABLED" = "true" ]
then
    echo "Ajax spider: enabled"
    options="$options --ajaxSpider"
else
    echo "Ajax spider: disabled"
fi

echo ""
echo "Start scanning"
echo ""

java -jar $TOOL_FOLDER/owaspzap-wrapper.jar $options --targetURL "$PDS_SCAN_TARGET_URL" --report "$PDS_JOB_RESULT_FILE"