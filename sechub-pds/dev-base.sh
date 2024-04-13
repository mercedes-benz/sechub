#!/bin/bash
# SPDX-License-Identifier: MIT

# For integration tests, local develoment etc. we need a generated, private key
# which is different for each developer, not accidently committed to git, also
# valid on builds etc.
#
# We support this by having a fixed target file path to a folder which is not tracked
# by git (except the README.md there). So even generated private keys are not accidently added
# to git...

DEV_CERT_PATH="$(pwd)/src/main/resources/certificates-untracked"
DEV_CERT_FILE="$DEV_CERT_PATH/generated-dev-localhost-keystore.p12"


PSEUDO_PWD="123456"

function createLocalhostCertifcate(){

    #
    # PRECONDITION: We assume a JDK is installed and so keytool is accessible!
    #
    # see https://stackoverflow.com/questions/13578134/how-to-automate-keystore-generation-using-the-java-keystore-tool-w-o-user-inter
    echo "Start creating localhost certificate"
    keytool -genkey -alias tomcat -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore "$DEV_CERT_FILE" -validity 3650 -storepass $PSEUDO_PWD --dname "CN=localhost, OU=ID"
    echo "Created file $DEV_CERT_FILE"

}

