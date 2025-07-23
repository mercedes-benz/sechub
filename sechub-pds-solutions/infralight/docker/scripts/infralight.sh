#!/bin/bash
# SPDX-License-Identifier: MIT

declare -r wrapper_infralight="$TOOL_FOLDER/sechub-wrapper-infralight.jar"

## Mock mode
if [[ "$PDS_INTEGRATIONTEST_ENABLED" = "true" ]] ; then
  echo "Integrationtest will be performed. Nmap will not be executed."
   
  # Execute the wrapper
  java -jar "$wrapper_infralight"
  
  exit $?
fi


export PDS_INFRALIGHT_PRODUCT_FOLDER="tool-results"
mkdir --parents "$PDS_INFRALIGHT_PRODUCT_FOLDER"

## Nmap
ips=$(echo $PDS_SCAN_CONFIGURATION | jq '.infraScan.ips[]')
if [ $? -eq 0 ] ; then
  echo $ips | sed 's/\"//g' >> hosts.txt
fi

if [ -f hosts.txt ]; then
  nmap -p- -iL hosts.txt -oX "$PDS_INFRALIGHT_PRODUCT_FOLDER/nmap-portscan-output.xml"
  rm hosts.txt
else
  echo "No targets were configured for port scan."
fi

## Wrapper call
java -jar "$wrapper_infralight"
