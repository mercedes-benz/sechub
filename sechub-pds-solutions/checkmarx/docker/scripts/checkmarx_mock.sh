#!/usr/bin/bash
# SPDX-License-Identifier: MIT

echo "################################"
echo "# Starting Checkmarx mock scan #"
echo "################################"
echo ""
echo "Returning default mock result."

cp "$MOCK_FOLDER/checkmarx-mockdata-multiple.xml" "$PDS_JOB_RESULT_FILE"