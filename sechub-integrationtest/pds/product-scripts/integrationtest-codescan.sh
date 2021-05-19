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
# 
cp "${PDS_JOB_SOURCECODE_UNZIPPED_FOLDER}/data.txt" ${PDS_JOB_RESULT_FILE}
 
# Now we add a "header" so identifyable by importer + synthetic info object to check params
echo "#PDS_INTTEST_PRODUCT_CODESCAN
info:pds.test.key.variantname as PDS_TEST_KEY_VARIANTNAME=$PDS_TEST_KEY_VARIANTNAME,product1.level as PRODUCT1_LEVEL=$PRODUCT1_LEVEL
$(cat ${PDS_JOB_RESULT_FILE})" > ${PDS_JOB_RESULT_FILE}