# Purpose of this directory
The purpose of this directory is to place a secretvalidation-wrapper .jar into here.

When the build is started with "copy" as BUILD_TYPE then the file
"sechub-wrapper-secretvalidation-${SECRETVALIDATION_WRAPPER_VERSION}.jar
will be copied into the container.

This way pds-gitleaks containers with a custon secretvalidation-wrapper .jar can be built.
