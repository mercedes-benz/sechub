#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

print_error() {
    message="$1"

    echo "$message" 1>&2
}

JAVA_DISTRIBUTION="$1"
JAVA_VERSION="$2"
JAVA_RUNTIME="$3"

JAVA_DIR="/opt/java"

if [ -z "$JAVA_DISTRIBUTION" ]
then
    print_error "ERROR: No Java distribution provided!"
    exit 1
fi

if [ -z "$JAVA_VERSION" ]
then
    print_error "ERROR: No Java version provided!"
    exit 1
fi

if [ "$JAVA_RUNTIME" != "jdk" ] &&  [ "$JAVA_RUNTIME" != "jre" ]
then
    print_error "ERROR: No Java runtime provided!"
    print_error "Possible values: jre and jdk"
    exit 1
fi

case "$JAVA_DISTRIBUTION" in
  openjdk)
    ./install-openjdk.sh "$JAVA_VERSION" "$JAVA_RUNTIME"
  ;;
  openj9)
    ./install-openj9.sh "$JAVA_VERSION" "$JAVA_RUNTIME" "$JAVA_DIR"
  ;;
  temurin)
    ./install-temurin.sh "$JAVA_VERSION" "$JAVA_RUNTIME"
  ;;
  *)
    print_error "Java distribution $JAVA_DISTRIBUTION not supported!"
    print_error "Possible values: openj9, openjdk, temurin"
    exit 1
  ;;
esac