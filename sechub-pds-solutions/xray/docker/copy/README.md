<!-- SPDX-License-Identifier: MIT --->
# Purpose of this directory
The purpose of this directory is to place a xray-wrapper .jar into here.

When the build is started with "copy" as BUILD_TYPE then the file
"sechub-wrapper-xray-${XRAY_WRAPPER_VERSION}.jar
will be copied into the container.

This way pds-xray containers with a custon xray-wrapper .jar can be built.
