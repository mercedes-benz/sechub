#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

echo "Starting up OWAS-ZAP server"
# -silent: disables telemetry calls, of the call home addon: https://www.zaproxy.org/docs/desktop/addons/call-home/
#   This addon is mandatory now but the telemetry calls can be deactivated.
#   This feature addtionally disables automated update calls, e.g. to update extensions.
#   Otherwise, if you want to use a specific versions of extensions e.g. for testing reasons, ZAP would automatically check for updates.
nohup owasp-zap -daemon -silent -nostdout -host 127.0.0.1 -port 8080 -config api.key="$ZAP_API_KEY" &
