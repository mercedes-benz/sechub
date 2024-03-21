#!/usr/bin/bash
# SPDX-License-Identifier: MIT

echo ""
echo "---------"
echo "PDS Setup"
echo "---------"
echo ""
echo "SecHub Job UUID: $SECHUB_JOB_UUID"
echo "PDS Job UUID: $PDS_JOB_UUID"
echo ""

echo "SECHUB_PREPARE_RESULT;status=ok" > "$PDS_JOB_RESULT_FILE"



