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
# TODO albert, 2020-07-06: use the parameters defined in server configuration to handle different
#                          e.g. we coulde simulate failures etc.
TARGET="$PDS_JOB_WORKSPACE_LOCATION/output/result.txt"
cp "$PDS_JOB_WORKSPACE_LOCATION/upload/unzipped/sourcecode/data.txt" $TARGET
 
# Now we add a "header" so identifyable by importer
echo "#PDS_INTTEST_PRODUCT_CODESCAN
$(cat $TARGET)" > $TARGET