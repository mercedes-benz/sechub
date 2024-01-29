#!/bin/bash 
# SPDX-License-Identifier: MIT

source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh

echo "PDS analytics integrationt test script starting..."

dumpPDSVariables

echo "Working directory"
pwd

# Inside the configuration file we have configured "sources" as accepted type
# This means, that this script is only started when sources are available!

echo "Check if sources are uploaded"
# we check if logic of preventing unnecessary uploads does not accidently constraint
# sources from being uploaded

if [[ "$PDS_JOB_HAS_EXTRACTED_SOURCES" = "false" ]]; then
	echo "No extracted sources found."
	errEcho "Illegal state: sources must be available."
	errEcho "- Upload logic failed! We said 'sources' are required for the product!"
	errEcho "- Means: either the product has source code available or it is not called"
	errEcho "  (Be aware, do not use this script for complete filtered files, because this"
	errEcho "   would fail here always.... )"
	
	
	exit 1
fi

## Return a CLOC file as a result
cp "./../sechub-integrationtest/src/test/resources/pds/analytics/cloc-output-1.json" "$PDS_JOB_RESULT_FILE"

