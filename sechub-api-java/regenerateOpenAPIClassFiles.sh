#!/bin/bash 

#
# You can call this script to update all files inside gen folder
# This will NOT regenerate wrapper classes.
cd ..
./gradlew :sechub-api-java:callOpenAPIJavaGenerator -Dsechub.build.stage=all 