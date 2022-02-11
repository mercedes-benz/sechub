#!/bin/bash
# SPDX-License-Identifier: MIT

download_location="https://github.com/mercedes-benz/sechub/releases/download"

function usage_and_exit(){
  cat - <<EOF
usage: $0 <product> <version>

Creates a HTML file to stdout containing a redirect to the download location of <product>/<version>.

- <product> is one of: 'client', 'pds', 'server'. (currently only 'client' is supported)
- <version> is the classical <major>.<minor>.<hotfix> notation

example: $0 client 1.0.0 > client-download.html
EOF
  exit
}

product="$1"
version="$2"

[ -z "$product" ] && usage_and_exit
[ -z "$version" ] && usage_and_exit

case "$product" in
  client)
    download_url="${download_location}/v${version}-client/sechub-cli-${version}.zip"
    ;;
  server)
    download_url="${download_location}/v${version}-server/sechub-server-${version}.jar"
    ;;
  pds)
    download_url="${download_location}/v${version}-pds/sechub-pds-${version}.jar"
    ;;
  *)
    echo "Product '$product' is not yet implemented."
    exit 1
    ;;
esac

# Create HTML
cat - <<EOF
<html>
<head>
   <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
   <meta http-equiv="refresh" content="0; URL=${download_url}">
   <title>Main Page</title>
</head>
<body></body>
</html>
EOF
