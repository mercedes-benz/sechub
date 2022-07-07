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

if [[ "$PDS_TEST_KEY_VARIANTNAME" != "f" ]]; then
    # Variant f does provide "lazy streams" with dedicated content and depends on output.
    # But for all other variants we can provide some additoinal information in messages     
    echo ">PDS_JOB_UUID=$PDS_JOB_UUID"
    echo ">PDS_TEST_KEY_VARIANTNAME=$PDS_TEST_KEY_VARIANTNAME"
    echo ">PDS_JOB_USER_MESSAGES_FOLDER=$PDS_JOB_USER_MESSAGES_FOLDER"
    echo ">PDS_JOB_HAS_EXTRACTED_SOURCES =$PDS_JOB_HAS_EXTRACTED_SOURCES"
    echo ">PDS_JOB_HAS_EXTRACTED_BINARIES=$PDS_JOB_HAS_EXTRACTED_BINARIES"
    echo ">INTEGRATION_TEST_DEBUG=$INTEGRATION_TEST_DEBUG"
fi

if [[ "$PDS_JOB_HAS_EXTRACTED_SOURCES" = "true" ]]; then
   mergeFolderFilesRecursivelyIntoResultFile "sources", $PDS_JOB_EXTRACTED_SOURCES_FOLDER ${PDS_JOB_RESULT_FILE} $INTEGRATION_TEST_DEBUG
fi

if [[ "$PDS_JOB_HAS_EXTRACTED_BINARIES" = "true" ]]; then
   mergeFolderFilesRecursivelyIntoResultFile "binaries" $PDS_JOB_EXTRACTED_BINARIES_FOLDER ${PDS_JOB_RESULT_FILE} $INTEGRATION_TEST_DEBUG
fi

# Now we add a "header" so identifyable by importer + synthetic info object to check params
echo "#PDS_INTTEST_PRODUCT_CODESCAN
info:pds.test.key.variantname as PDS_TEST_KEY_VARIANTNAME=$PDS_TEST_KEY_VARIANTNAME,product1.level as PRODUCT1_LEVEL=$PRODUCT1_LEVEL
$(cat ${PDS_JOB_RESULT_FILE})" > ${PDS_JOB_RESULT_FILE}

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
    errEcho "ERROR message before doing an exit 1..."
    exit 1
fi

if [[ "$PDS_TEST_KEY_VARIANTNAME" = "" ]]; then
    echo "No variant found - so direct PDS test"
    
    infoMessage "info for PDS job: $PDS_JOB_UUID"
    warnMessage "warn for PDS job: $PDS_JOB_UUID"
    errorMessage "error for PDS job: $PDS_JOB_UUID but with
    a multine ....
    "
    
    echo "After messages were created I found this inside messages folder:"
    echo "----------------------------------------------------------------------------"
    ls $PDS_JOB_USER_MESSAGES_FOLDER 
    echo "----------------------------------------------------------------------------"
    
    # For direct pds tests, we create a simple metadata.txt when executed:
    echo "generated meta data for PDS job:$PDS_JOB_UUID" > $PDS_JOB_METADATA_FILE
    echo "> Meta data was written..."
    echo "> PDS_JOB_METADATA_FILE=$PDS_JOB_METADATA_FILE"
    
fi
