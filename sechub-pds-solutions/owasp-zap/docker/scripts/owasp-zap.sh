#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

sechub_job_uuid=$(echo "$PDS_JOB_WORKSPACE_LOCATION" | cut -d "/" -f 4)

echo "Clean up possible old session data before starting sechub job: $sechub_job_uuid"
rm -r ~/.ZAP/session/*
rm ~/.ZAP/zap.log*

echo "sechub job uuid: $sechub_job_uuid"
echo ""

echo "###########################"
echo "# Starting OWASP ZAP scan #"
echo "###########################"
echo ""
echo "Target URL: $PDS_SCAN_TARGET_URL"
echo "Target Type: $PDS_SCAN_TARGET_TYPE"
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

if [ "$ZAP_USE_PROXY" != "true" ] && [ "$ZAP_PROXY_FOR_PDS_TARGET_TYPE" = "$PDS_SCAN_TARGET_TYPE" ]
then
    ZAP_USE_PROXY="true"
    echo "Using proxy for target type: $PDS_SCAN_TARGET_TYPE"
fi

if [ "$ZAP_USE_PROXY" = "true" ]
then
    echo "Use proxy: enabled"
    options="$options --proxyHost $ZAP_PROXY_HOST --proxyPort $ZAP_PROXY_PORT"
else
    echo "Use proxy: disabled"
fi

echo ""
echo "Start scanning"
echo ""

if [ ! -z "$PDS_SCAN_CONFIGURATION" ]
then
    sechub_scan_configuration="$PDS_JOB_WORKSPACE_LOCATION/sechubScanConfiguration.json"
    
    echo "Using configuration file: $sechub_scan_configuration"
    
    echo "$PDS_SCAN_CONFIGURATION" > "$sechub_scan_configuration"

    options="$options --sechubConfigfile $sechub_scan_configuration"
fi

java -jar $TOOL_FOLDER/wrapperowaspzap.jar $options --zapHost 127.0.0.1 --jobUUID "$sechub_job_uuid" --zapPort 8080 --targetURL "$PDS_SCAN_TARGET_URL" --report "$PDS_JOB_RESULT_FILE" --fullRulesetfile $TOOL_FOLDER/owasp-zap-full-ruleset-all-release-status.json
