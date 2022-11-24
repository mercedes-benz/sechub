#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

shutdownZAP() {
	# -f to specify the process by looking at the parameters
	pkill -9 -f "/usr/bin/owasp-zap"
	pkill -9 -f "/usr/share/zaproxy/zap-"
}

# Start OWASP-ZAP server
echo "Starting up OWASP-ZAP server"
# -silent: disables telemetry calls, of the call home addon: https://www.zaproxy.org/docs/desktop/addons/call-home/
#   This addon is mandatory now but the telemetry calls can be deactivated.
#   This feature addtionally disables automated update calls, e.g. to update extensions.
#   Otherwise, if you want to use a specific versions of extensions e.g. for testing reasons, ZAP would automatically check for updates.
owasp-zap -daemon -silent -nostdout -host $ZAP_HOST -port $ZAP_PORT -config api.key=$ZAP_API_KEY &

echo "Waiting for OWASP-ZAP to start"
wget --quiet --output-document=- --retry-connrefused --tries=20 --waitretry=6 --header="Accept: application/json" --header="X-ZAP-API-Key: $ZAP_API_KEY" http://$ZAP_HOST:$ZAP_PORT/JSON/core/view/version
if [ $? -ne 0 ]
then
    echo "OWASP-ZAP did not start after waiting for 2 minutes"
    shutdownZAP
    exit 1
fi
echo "OWASP-ZAP started"

# Execute scan
sechub_job_uuid=$(echo "$PDS_JOB_WORKSPACE_LOCATION" | cut -d "/" -f 4)

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

java -jar $TOOL_FOLDER/wrapperowaspzap.jar $options --zapHost $ZAP_HOST --zapPort $ZAP_PORT --zapApiKey $ZAP_API_KEY --jobUUID "$sechub_job_uuid" --targetURL "$PDS_SCAN_TARGET_URL" --report "$PDS_JOB_RESULT_FILE" --fullRulesetfile $TOOL_FOLDER/owasp-zap-full-ruleset-all-release-status.json


# Shutdown OWASP-ZAP and cleanup after the scan
echo "Shutdown OWASP-ZAP after scan"
shutdownZAP

echo "Clean up possible old session data after scan"
rm -r ~/.ZAP/session/*
rm ~/.ZAP/zap.log*