#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

JAVA_VERSION="$1"
JAVA_RUNTIME="$2"
JAVA_DIR="$3"

java_url=""

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install --assume-yes --quiet wget

case "$JAVA_VERSION" in
  11)
    java_url="https://github.com/ibmruntimes/semeru11-binaries/releases/download/jdk-11.0.17%2B8_openj9-0.35.0/ibm-semeru-open-${JAVA_RUNTIME}_x64_linux_11.0.17_8_openj9-0.35.0.tar.gz"
  ;;
  17)
    java_url="https://github.com/ibmruntimes/semeru17-binaries/releases/download/jdk-17.0.5%2B8_openj9-0.35.0/ibm-semeru-open-${JAVA_RUNTIME}_x64_linux_17.0.5_8_openj9-0.35.0.tar.gz"
  ;;
  18)
    java_url="https://github.com/ibmruntimes/semeru18-binaries/releases/download/jdk-18.0.2%2B9_openj9-0.33.0/ibm-semeru-open-${JAVA_RUNTIME}_x64_linux_18.0.2_9_openj9-0.33.0.tar.gz"
  ;;
  *)
    echo "Java $JAVA_VERSION not supported!" 1>&2
    exit 1
  ;;
esac

wget --no-verbose "$java_url"
wget --no-verbose "${java_url}.sha256.txt"

sha256sum --check ibm-semeru-open-*.tar.gz.sha256.txt
mkdir --parents "$JAVA_DIR"
tar --extract --gzip --file ibm-semeru-open-*.tar.gz --directory="$JAVA_DIR"
rm ibm-semeru-open-*.tar.gz*

# link to java installation
ln --symbolic $JAVA_DIR/j*/bin/java /usr/bin/java

# link to keytool installation
ln --symbolic $JAVA_DIR/j*/bin/keytool /usr/bin/keytool

apt-get remove --assume-yes --quiet wget
apt-get clean