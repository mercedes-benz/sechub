#!/bin/sh
# SPDX-License-Identifier: MIT

debug () {
    while true
    do
	    echo "Press [CTRL+C] to stop.."
	    sleep 120
    done
}

if [ "$LOADBALANCER_START_MODE" != "server" ]
then
    debug
fi

echo "Check configuration file"
nginx -t

echo "Start Nginx"
nginx -g 'daemon off;'