#!/bin/bash
# SPDX-License-Identifier: MIT

ARG="$1"
JAVA_API_JAR_TO_COPY="sechub-api-java/build/libs/sechub-java-api-all-0.0.0.jar"
JAVA_API_JAR_TO_CREATE="ide-plugins/jetbrains/lib/sechub-java-api-all-0.0.0.jar"

cd "../../"

if [ ! -f "$JAVA_API_JAR_TO_CREATE" ] || [ "$ARG" = "rebuildJar" ]; then
    ./gradlew buildJavaApiAll
    cp "$JAVA_API_JAR_TO_COPY" "$JAVA_API_JAR_TO_CREATE"
fi

cd "-"