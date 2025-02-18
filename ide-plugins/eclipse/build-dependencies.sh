#!/bin/bash
# SPDX-License-Identifier: MIT

set -e

ARG="$1"
JAVA_API_JAR_TO_COPY="sechub-api-java/build/libs/sechub-java-api-all-0.0.0.jar"
JAVA_API_JAR_TO_CREATE="ide-plugins/eclipse/sechub-eclipse-plugin/lib/sechub-java-api-all-0.0.0.jar"

cd "../../"

if [ ! -f "$JAVA_API_JAR_TO_CREATE" ] || [ "$ARG" = "rebuildJar" ]; then
    echo "[BUILD]: Building $JAVA_API_JAR_TO_COPY."
    ./gradlew :sechub-api-java:build
    ./gradlew :sechub-api-java:buildJavaApiAll
    echo "[BUILD]: Copying $JAVA_API_JAR_TO_COPY to $JAVA_API_JAR_TO_CREATE."
    cp "$JAVA_API_JAR_TO_COPY" "$JAVA_API_JAR_TO_CREATE"
else
    echo "[SKIPPED]: Building $JAVA_API_JAR_TO_COPY was skipped."
fi

cd "-"