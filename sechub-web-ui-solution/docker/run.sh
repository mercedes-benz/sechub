#!/bin/sh
# SPDX-License-Identifier: MIT
set -e

debug () {
  while true ; do
    echo "Press [CTRL+C] to stop.."
    sleep 120
  done
}

install_ssl_certs () {
  echo "### Installing SSL certs \"$WEB_UI_SSL_KEYSTORE_ALIAS\""
  K8S_SSL_SECRETS="/sechub-web-ui/secrets/secret-ssl"

  cd "$CERTIFICATE_DIRECTORY"

  echo "# Extraxting private key"
  openssl pkcs12 -in "$K8S_SSL_SECRETS/keystore_file" -nocerts -out key.pem -nodes -legacy -password file:"$K8S_SSL_SECRETS/keystore_password"

  echo "# Extracting certificate(s)"
  openssl pkcs12 -in "$K8S_SSL_SECRETS/keystore_file" -nokeys -out cert.pem -nodes -legacy -password file:"$K8S_SSL_SECRETS/keystore_password"

  echo "# Verifying name (keystore alias)"
  grep "friendlyName: $WEB_UI_SSL_KEYSTORE_ALIAS" key.pem
  grep "friendlyName: $WEB_UI_SSL_KEYSTORE_ALIAS" cert.pem

  echo "# Replacing certificates"
  cat key.pem | sed -ne '/-BEGIN PRIVATE KEY-/,/-END PRIVATE KEY-/p' > sechub-web-ui.key
  cat cert.pem | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > sechub-web-ui.cert

  # cleanup temporary files
  rm -f key.pem cert.pem
}

###############
# main
if [ "$WEB_UI_SSL_KEYSTORE_ALIAS" != "undefined" ] ; then
  install_ssl_certs
fi

if [ "$LOADBALANCER_START_MODE" != "server" ] ; then
  debug
fi

echo "### Checking configuration file"
nginx -t

echo "### Starting Nginx"
nginx -g 'daemon off;'
