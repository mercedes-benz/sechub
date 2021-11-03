#!/bin/bash 

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

function errEcho () {
    echo "$@" >&2
}

function produceLargerOutputStreamContent() {
    x=1
    while [ $x -le 50 ]
    do
      echo "Just some larger output stream content - line $x"
      x=$(( $x + 1 ))
    done
}


cp "${PDS_JOB_SOURCECODE_UNZIPPED_FOLDER}/data.txt" ${PDS_JOB_RESULT_FILE}
 
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