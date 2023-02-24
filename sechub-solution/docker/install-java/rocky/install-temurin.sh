#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

JAVA_VERSION="$1"
JAVA_RUNTIME="$2"

microdnf update --assumeyes
microdnf install --assumeyes wget



# Distribution name centos/rhel/fedora/rocky etc.
# For more information: https://packages.adoptium.net/ui/native/rpm/
# NOTE: There are currently no packages for RockyLinux 9, therefore use rhel
DISTRIBUTION_NAME=rhel

cat <<EOF > /etc/yum.repos.d/adoptium.repo
[Adoptium]
name=Adoptium
baseurl=https://packages.adoptium.net/artifactory/rpm/${DISTRIBUTION_NAME:-$(. /etc/os-release; echo $ID)}/\$releasever/\$basearch
enabled=1
gpgcheck=1
gpgkey=https://packages.adoptium.net/artifactory/api/gpg/key/public
EOF

microdnf update --assumeyes
microdnf install --assumeyes "temurin-17-${JAVA_RUNTIME}"
microdnf clean all