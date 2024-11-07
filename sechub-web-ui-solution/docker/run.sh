#!/bin/sh
# SPDX-License-Identifier: MIT

debug () {
  while true ; do
    echo "Press [CTRL+C] to stop.."
    sleep 120
  done
}

install_ssl_certs () {
  echo "### Installing SSL certs \"$WEB_UI_SSL_KEYSTOREALIAS\""
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
