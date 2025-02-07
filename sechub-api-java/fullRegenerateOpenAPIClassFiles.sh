#!/bin/bash 
# SPDX-License-Identifier: MIT

#
# You can call this script to update all files inside gen folder
# This will NOT regenerate wrapper classes.
# But it will regenerate the open api file (rest doc tests are executed as well) and 
# generated the OpenApi gen parts completely.
# 
# If there are changes on wrapper side necessary you have to call SystemTestManualLocalServerIntTest
# again with necessary system properties defined!
set -e

echo "-----------------------------------"
echo "Regenerate OpenAPI class files"
echo "-----------------------------------"
echo "Attention:"
echo "**********"
echo "For developers on local machines: If you change RestDoc tests to change OpenAPi"
echo "generation, you have to"
echo ""
echo "- start the corresponding {name}RestDocTest to provide meta data"
echo "- execute this script"
echo ""
echo "This script does:"
echo "- apply spotless"
echo "- generate open api file"
echo "- generate sechub java api"

cd ..
echo ">> Generate OpenAPI (sechub-openapi-java-client)"
./gradlew spotlessApply generateOpenapi

echo ">> Generate OpenAPI (sechub-api-java) [DEPRECATED]"
./gradlew :sechub-api-java:build  -Dsechub.build.stage=all 