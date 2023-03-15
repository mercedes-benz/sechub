#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

JAVA_VERSION="$1"
JAVA_RUNTIME="$2"

dnf update --assumeyes
dnf install --assumeyes wget



# Distribution name centos/rhel/fedora/rocky etc.
# For more information: https://packages.adoptium.net/ui/native/rpm/
DISTRIBUTION_NAME=fedora

cat <<EOF > /etc/yum.repos.d/adoptium.repo
[Adoptium]
name=Adoptium
baseurl=https://packages.adoptium.net/artifactory/rpm/${DISTRIBUTION_NAME:-$(. /etc/os-release; echo $ID)}/\$releasever/\$basearch
enabled=1
gpgcheck=1
gpgkey=https://packages.adoptium.net/artifactory/api/gpg/key/public
EOF

dnf update --assumeyes
dnf install --assumeyes "temurin-17-${JAVA_RUNTIME}"
dnf clean all