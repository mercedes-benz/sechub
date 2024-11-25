#!/bin/bash
# SPDX-License-Identifier: MIT
set -e

# This script creates Debian packages of the SecHub client for Linux
# It is meant to be used for SecHub client releases

# Debian packaging data
DEB_PACKAGE_NAME="sechub-client"
DEB_SECTION="misc"
DEB_MAINTAINER="SecHub FOSS team <sechub@example.org>"
DEB_HOMEPAGE="https://github.com/mercedes-benz/sechub"
DEB_DESCRIPTION="The SecHub command line client. See $DEB_HOMEPAGE"
DEB_PATH="usr/bin" # Where to place the SecHub client executable on install

# Hardware architectures we build Debian packages for
ARCHITECTURE_LIST="amd64 i386 arm arm64" # space separated list

BUILD_DIR="build"
DEBIAN_BUILD_DIR="deb-build"
GO_BUILD_DIR="go/platform"
MANDATORY_EXECUTABLES="dpkg-deb fakeroot" # space separated list


function check_executable_is_installed {
    executable="$1"
    exe_path=`which $executable`
    if [ ! -x "$exe_path" ] ; then
        echo "FATAL: Mandatory executable \"$executable\" not found in PATH. Please install..."
        exit 1
    fi
}

function prepare_package_build {
  local architecture="$1"
  local deb_dir="$DEBIAN_BUILD_DIR/$architecture"
  local size
  mkdir -p "$deb_dir/DEBIAN" "$deb_dir/$DEB_PATH"
  cp "$GO_BUILD_DIR/linux-$architecture/sechub" "$deb_dir/$DEB_PATH"
  # determine file size in bytes
  size=`cat "$deb_dir/$DEB_PATH/sechub" | wc --bytes`
  cat - <<EOF > "$deb_dir/DEBIAN/control"
Package: $DEB_PACKAGE_NAME
Version: $SECHUB_CLIENT_VERSION
Section: $DEB_SECTION
Architecture: $architecture
Priority: optional
Essential: no
Installed-Size: $size
Homepage: $DEB_HOMEPAGE
Maintainer: $DEB_MAINTAINER
Description: $DEB_DESCRIPTION
EOF
}


################
SECHUB_CLIENT_VERSION="$1"
echo "ToDo: verify"

# Check prepreqs
for i in $MANDATORY_EXECUTABLES ; do
    check_executable_is_installed $i
done

cd `dirname $0/..`
if [ ! -d "$BUILD_DIR/$GO_BUILD_DIR" ] ; then
  echo "Please build the SecHub client executables first. './gradlew buildGo testGo'"
  exit 1
fi

cd "$BUILD_DIR"
mkdir -p "$DEBIAN_BUILD_DIR"
for arch in $ARCHITECTURE_LIST ; do
  prepare_package_build $arch
  build_deb_package $arch
done
