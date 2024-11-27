#!/bin/bash
# SPDX-License-Identifier: MIT
set -e

# Debian packaging data
DEB_PACKAGE_NAME="sechub-client"
DEB_SECTION="misc"
DEB_MAINTAINER="SecHub FOSS team <sechub@example.org>"
DEB_HOMEPAGE="https://github.com/mercedes-benz/sechub"
DEB_DESCRIPTION="The SecHub command line client. See $DEB_HOMEPAGE"
DEB_BIN_PATH="usr/bin" # Where to place the SecHub client executable on install

# Hardware architectures we build Debian packages for
ARCHITECTURE_LIST="amd64 386 arm arm64" # space separated list

BUILD_DIR="build"
DEBIAN_BUILD_DIR="deb-build"
GO_BUILD_DIR="go/platform"
MANDATORY_EXECUTABLES="dpkg-deb fakeroot" # space separated list

function usage {
  cat - <<EOF

usage: $0 <version tag>

This script creates Debian packages of the SecHub client for Linux
It is meant to be used for SecHub client releases

Mandatory argument is the version tag in format <major>.<minor>.<hotfix> with an optional appendix.
Examples:
- 1.10.0
- 1.10.0-gh-build
- 1.10.0-9
EOF
}

function check_executable_is_installed {
    executable="$1"
    exe_path=`which $executable`
    if [ ! -x "$exe_path" ] ; then
        echo "FATAL: Mandatory executable \"$executable\" not found in PATH. Please install..."
        exit 1
    fi
}

function get_debian_architecture {
  local deb_architecture
  # Special case for i386 architecture
  if [ "$1" = "386" ] ; then
    deb_architecture="i386"
  else
    deb_architecture="$architecture"
  fi
  echo $deb_architecture
}

function build_deb_package {
  local architecture="$1"
  local deb_architecture=`get_debian_architecture $architecture`
  local deb_package_name="sechub-client_${SECHUB_CLIENT_VERSION}_${deb_architecture}"
  local deb_dir="$DEBIAN_BUILD_DIR/$deb_package_name"
  local size
  echo "### Building Debian package $deb_package_name.deb"
  # create dirs
  mkdir -p "$deb_dir/DEBIAN" "$deb_dir/$DEB_BIN_PATH"
  # copy executable into destination dir
  cp "$GO_BUILD_DIR/linux-$architecture/sechub" "$deb_dir/$DEB_BIN_PATH"
  # determine file size in bytes
  size=`cat "$deb_dir/$DEB_BIN_PATH/sechub" | wc --bytes`
  # Create Debian package meta data
  cat - <<EOF > "$deb_dir/DEBIAN/control"
Package: $DEB_PACKAGE_NAME
Version: $SECHUB_CLIENT_VERSION
Section: $DEB_SECTION
Architecture: $deb_architecture
Priority: optional
Essential: no
Installed-Size: $size
Homepage: $DEB_HOMEPAGE
Maintainer: $DEB_MAINTAINER
Description: $DEB_DESCRIPTION
EOF
  # Create Debian package
  fakeroot dpkg-deb --build "$deb_dir"
}

################

# Check prepreqs
for i in $MANDATORY_EXECUTABLES ; do
    check_executable_is_installed $i
done

SECHUB_CLIENT_VERSION=$1

FAILED=false
if [ -z "$SECHUB_CLIENT_VERSION" ] ; then
  echo "Please provide a version tag as 1st argument"
  FAILED=true
elif [[ ! "$SECHUB_CLIENT_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+ ]]; then
  echo "Provided version tag is invalid"
  FAILED=true
fi

cd `dirname $0`/..
if [ ! -d "$BUILD_DIR/$GO_BUILD_DIR" ] ; then
  echo "Please build the SecHub client executables first. './gradlew buildGo testGo'"
  FAILED=true
fi

if $FAILED ; then
  usage
  exit 1
fi

cd "$BUILD_DIR"
mkdir -p "$DEBIAN_BUILD_DIR"
for arch in $ARCHITECTURE_LIST ; do
  build_deb_package $arch
done
