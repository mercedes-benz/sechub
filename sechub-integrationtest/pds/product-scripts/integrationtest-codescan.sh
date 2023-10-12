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
# This "test product" does just return data.txt as the result so just doing a copy thats all
#
# PDS_JOB_WORKSPACE_LOCATION is a special variable and points always to job workspace location
# PDS_JOB_RESULT_FILE is a special variable and points directly to result file
#

# Info: 
# we are inside "sechub-pds" sub folder at execution time, because the PDS server instance runs her...
# so we must source from other location...
#
# 
# Remark: This file is automatically started by a Junit test: SharedFunctionScriptTest. So it 
#         is part of the build process.
#
set -e
source ./../sechub-integrationtest/pds/product-scripts/shared-functions.sh

function produceLargerOutputStreamContent() {
    x=1
    while [ $x -le 50 ]
    do
      echo "Just some larger output stream content - line $x"
      x=$(( $x + 1 ))
    done
}

#
# Dump PDS variables
#
if [[ "$PDS_TEST_KEY_VARIANTNAME" != "f" ]]; then
    # Variant f does provide "lazy streams" with dedicated content and depends on output.
    # But for all other variants we can provide some additoinal information in messages     
    dumpPDSVariables   
fi

if [[ "$PDS_TEST_KEY_VARIANTNAME" = "j" ]]; then
    dumpVariable "TEST_MAPPING1_REPLACE_PROJECT1"
    dumpVariable "TEST_MAPPING2_NOT_EXISTING_IN_SECHUB"
fi

#
# Handle extreaction
#
if [[ "$PDS_JOB_HAS_EXTRACTED_SOURCES" = "true" ]]; then
   mergeFolderFilesRecursivelyIntoResultFile "sources", "$PDS_JOB_EXTRACTED_SOURCES_FOLDER" "${PDS_JOB_RESULT_FILE}" "$PDS_DEBUG_ENABLED"
fi

if [[ "$PDS_JOB_HAS_EXTRACTED_BINARIES" = "true" ]]; then
   mergeFolderFilesRecursivelyIntoResultFile "binaries" "$PDS_JOB_EXTRACTED_BINARIES_FOLDER" "${PDS_JOB_RESULT_FILE}" "$PDS_DEBUG_ENABLED"
fi

# Now we add a "header" so identifyable by importer + synthetic info object to check params
if [[ ! -f "${PDS_JOB_RESULT_FILE}" ]]; then
    touch "${PDS_JOB_RESULT_FILE}"
    echo "${PDS_JOB_RESULT_FILE} was missing - created empty file"
fi

# *******************************************
#        Post processing of result file
# *******************************************
#
# In the steps before we created a report file. The first step for the report is that the 
# unit test does upload a archive file (zip or tar) which contains a directory structure with different simple
# text files. The content of each file consists of single text lines. Each line represents a simple
# definition for a pseudo vulnerability. After the archive file is extracted by the PDS, the content of the 
# files were merged to one single report file.
#
# Furthermore, we have a special integration test importer in SecHub which can import such reports if they
# contain the marker #PDS_INTTEST_PRODUCT_CODESCAN in the first line.
#
# The second line is an additional pseudo vulnerability which is always inside the SecHub report.
# It is at info level and contains the parameters `pds.test.key.variantname` and `product1.level` as description.
# The variant name is used inside the script to have dedicated behaviors for different product executor configurations.
# For debugging purposes the developer can look into the sechub report and see which variant was used for this test.

mv "${PDS_JOB_RESULT_FILE}" "${PDS_JOB_RESULT_FILE}_tmp"
 
echo "#PDS_INTTEST_PRODUCT_CODESCAN
info:pds.test.key.variantname as PDS_TEST_KEY_VARIANTNAME=$PDS_TEST_KEY_VARIANTNAME,product1.level as PRODUCT1_LEVEL=$PRODUCT1_LEVEL" > "${PDS_JOB_RESULT_FILE}"
 
cat "${PDS_JOB_RESULT_FILE}_tmp" >> "${PDS_JOB_RESULT_FILE}"
 
rm "${PDS_JOB_RESULT_FILE}_tmp"

# - End of result file post processing

if [[ "$PDS_TEST_KEY_VARIANTNAME" = "f" ]]; then
    produceLargerOutputStreamContent
    echo $(date)
    echo "STARTING" 
    errEcho $(date)
    errEcho "NO-PROBLEMS"
    
    sleep 1s
    echo $(date)
    echo "WORKING1" 
    errEcho $(date)
    errEcho "ERRORS1"
    
    sleep 1s
    echo $(date)
    echo "WORKING2"
    errEcho $(date) 
    errEcho "ERRORS2"
    
    sleep 1s
    echo $(date)
    echo "WORKING3" 
    errEcho $(date)
    errEcho "ERRORS3"
fi

if [[ "$PDS_TEST_KEY_VARIANTNAME" = "g" ]]; then
    errEcho $(date)
    errEcho "ERROR output before doing an exit 1..."
    exit 1
fi

if [[ "$PDS_TEST_KEY_VARIANTNAME" = "k" ]]; then
    infoMessage "script is starting to inspect event folder: $PDS_JOB_EVENTS_FOLDER"

    counter=0
    if waitForEventAndSendMessage "cancel_requested" 0.3 30 ; then
       exit 0
    else
       exit 5    
    fi
fi


if [[ "$PDS_TEST_KEY_VARIANTNAME" = "" ]]; then
    echo "No variant found - so direct PDS test"
    
    infoMessage "info for PDS job: $PDS_JOB_UUID"
    warnMessage "warn for PDS job: $PDS_JOB_UUID"
    errorMessage "error for PDS job: $PDS_JOB_UUID but with
    a multine ....
    "
    
    echo "After messages were created, I found this inside messages folder:"
    echo "----------------------------------------------------------------------------"
    ls "$PDS_JOB_USER_MESSAGES_FOLDER" 
    echo "----------------------------------------------------------------------------"
    
    # For direct pds tests, we create a simple metadata.txt when executed:
    echo "generated meta data for PDS job:$PDS_JOB_UUID" > "$PDS_JOB_METADATA_FILE"
    echo "> Meta data was written..."
    echo "> PDS_JOB_METADATA_FILE=$PDS_JOB_METADATA_FILE"
    
fi
