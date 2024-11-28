#!/bin/bash
# SPDX-License-Identifier: MIT

shutdownZAP() {
	# --full: to specify the process by looking at full command line including the parameters
	pkill -9 --full "/pds/tools/ZAP_"
}

function check_env_var_is_set {
  local param="$1"
  if [ -z "${!param}" ] ; then
    echo "Mandatory environment variable $param is not set!"
    failed=true
  fi
}

# Check if mandatory environment variables are set
MANDATORY_ENV_VARS="PDS_SCAN_TARGET_TYPE PDS_SCAN_TARGET_URL SECHUB_JOB_UUID ZAP_ACTIVESCAN_ENABLED ZAP_AJAXCRAWLER_ENABLED ZAP_API_KEY ZAP_HOST ZAP_JVM_ARGS ZAP_PORT"
failed=false
for i in $MANDATORY_ENV_VARS ; do
  check_env_var_is_set $i
done
if $failed ; then
  echo "Please make sure that mandatory environment variables are passed to this script. Exiting."
  exit 1
fi

# Start OWASP-ZAP server
echo "Starting up OWASP-ZAP server"
# -silent: disables telemetry calls, of the call home addon: https://www.zaproxy.org/docs/desktop/addons/call-home/
#   This addon is mandatory now but the telemetry calls can be deactivated.
#   This feature addtionally disables automated update calls, e.g. to update extensions.
#   Otherwise, if you want to use a specific versions of extensions e.g. for testing reasons, ZAP would automatically check for updates.
zap "$ZAP_JVM_ARGS" -daemon -silent -nostdout -host "$ZAP_HOST" -port "$ZAP_PORT" -config "api.key=$ZAP_API_KEY" &

echo "Waiting for OWASP-ZAP to start"
RETRIES=20
SECONDS_BEFORE_RETRY=6
TOTAL_WAITTIME=$(($RETRIES*$SECONDS_BEFORE_RETRY))

# To check if OWASP-ZAP is up and running we try to call api for the OWASP-ZAP version
# --quiet: suppress the default output of wget
# --output-document: specify where to save the output to, "--output-document=-" specifies STDOUT
# --retry-connrefused: retry if connection was refused
# --tries: how often the connection shall be retried
# --waitretry: maximum amount of seconds before retrying to connect
# --header: headers needed for the request
# --no-check-certificate: OWASP-ZAP uses a self-signed certificate, because of this we skip the certificate validation
wget --quiet --output-document=- --retry-connrefused --tries="$RETRIES" --waitretry="$SECONDS_BEFORE_RETRY" --header="Accept: application/json" --header="X-ZAP-API-Key: $ZAP_API_KEY" "http://$ZAP_HOST:$ZAP_PORT/JSON/core/view/version"

if [ $? -ne 0 ] ; then
    echo "OWASP-ZAP did not start after waiting for $TOTAL_WAITTIME seconds"
    shutdownZAP
    exit 1
fi
echo "OWASP-ZAP started"
# Execute scan
echo "sechub job uuid: $SECHUB_JOB_UUID"
echo ""

options=""

if [[ "$PDS_WRAPPER_REMOTE_DEBUGGING_ENABLED" = "true" ]] ; then
    options="$options -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000"
fi

echo "###########################"
echo "# Starting OWASP ZAP scan #"
echo "###########################"
echo ""
echo "Target URL: $PDS_SCAN_TARGET_URL"
echo "Target Type: $PDS_SCAN_TARGET_TYPE"
echo ""

zap_options=""

if [ "$ZAP_ACTIVESCAN_ENABLED" = "true"  ] ; then
    echo "Active scan: enabled"
    zap_options="$zap_options --activeScan"
else
    echo "Active scan: disabled"
fi

if [ "$ZAP_AJAXCRAWLER_ENABLED" = "true" ] ; then
    echo "Ajax spider: enabled"
    zap_options="$zap_options --ajaxSpider"
else
    echo "Ajax spider: disabled"
fi

if [ "$ZAP_USE_PROXY" != "true" ] && [ "$ZAP_PROXY_FOR_PDS_TARGET_TYPE" = "$PDS_SCAN_TARGET_TYPE" ] ; then
    ZAP_USE_PROXY="true"
    echo "Using proxy for target type: $PDS_SCAN_TARGET_TYPE"
fi

if [ "$ZAP_USE_PROXY" = "true" ] ; then
    echo "Use proxy: enabled"
    zap_options="$zap_options --proxyHost $ZAP_PROXY_HOST --proxyPort $ZAP_PROXY_PORT"
else
    echo "Use proxy: disabled"
fi

if [ "$WRAPPER_CONNECTIONCHECK_ENABLED" = "true" ] ; then
    echo "Wrapper connection check: enabled"
    zap_options="$zap_options --connectionCheck"
else
    echo "Wrapper connection check: disabled"
fi

if [ ! -z "$WRAPPER_MAXIMUM_CONNECTION_RETRIES" ] ; then
    echo "Use WRAPPER_MAXIMUM_CONNECTION_RETRIES: $WRAPPER_MAXIMUM_CONNECTION_RETRIES"
    zap_options="$zap_options --maxNumberOfConnectionRetries $WRAPPER_MAXIMUM_CONNECTION_RETRIES"
else
    echo "Use default value of wrapper for WRAPPER_MAXIMUM_CONNECTION_RETRIES"
fi

if [ ! -z "$WRAPPER_RETRY_WAITTIME_MILLISECONDS" ] ; then
    echo "Use WRAPPER_RETRY_WAITTIME_MILLISECONDS: $WRAPPER_RETRY_WAITTIME_MILLISECONDS"
    zap_options="$zap_options --retryWaittimeInMilliseconds $WRAPPER_RETRY_WAITTIME_MILLISECONDS"
else
    echo "Use default value of wrapper for WRAPPER_RETRY_WAITTIME_MILLISECONDS"
fi

echo ""
echo "Start scanning"
echo ""

java -jar $options "$TOOL_FOLDER/wrapperowaspzap.jar" $zap_options --zapHost "$ZAP_HOST" --zapPort "$ZAP_PORT" --zapApiKey "$ZAP_API_KEY" --jobUUID "$SECHUB_JOB_UUID" --targetURL "$PDS_SCAN_TARGET_URL" --report "$PDS_JOB_RESULT_FILE" --fullRulesetfile "$TOOL_FOLDER/owasp-zap-full-ruleset-all-release-status.json"

# Shutdown OWASP-ZAP and cleanup after the scan
echo "Shutdown OWASP-ZAP after scan"
shutdownZAP

echo "Clean up possible old session data after scan"
rm -rf ~/.ZAP/session/*
rm ~/.ZAP/zap.log*
