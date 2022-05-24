#!/bin/bash 
# SPDX-License-Identifier: MIT

# "${pwd}/output/result.txt" is the default target for every PDS job!
#
# error and ouput results are automatically
# "${pwd}/output/system-err.log 
# "${pwd}/output/system-out.log
# written
#
# having configured to automatically unzip content, the uploaded zip file will be at
# "${pwd}/unzipped/sourcecode/*" in our case "${pwd}/unzipped/sourcecode/data.txt" 
# This "test product" does just return returned_sarif_result.json" as the result
# so just doing a copy thats all
#
# PDS_JOB_WORKSPACE_LOCATION is a special variable and points always to job workspace location
# PDS_JOB_RESULT_FILE is a special variable and points directly to result file

echo "PDS SARIF integrationt test script starting..."
echo ">PDS_JOB_HAS_EXTRACTED_SOURCES =$PDS_JOB_HAS_EXTRACTED_SOURCES"
echo ">PDS_JOB_HAS_EXTRACTED_BINARIES=$PDS_JOB_HAS_EXTRACTED_BINARIES"

cp "$PDS_JOB_EXTRACTED_SOURCES_FOLDER/returned_sarif_result.json" $PDS_JOB_RESULT_FILE