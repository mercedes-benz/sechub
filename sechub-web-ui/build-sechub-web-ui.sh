# SPDX-License-Identifier: MIT
cd `dirname $0`

# Check if npm is installed
which npm
if [ $? -ne 0 ] ; then
  echo "Web-Ui build relies on npm. Please install npm / Node.js"
  exit 1
fi

set -e

# check if openapi client is built
if [ ! -f ../sechub-openapi-ts-client/dist/gen/index.js ] ; then
  echo "OpenAPI client is not built. Generating OpenAPI client..."
  cd ../sechub-openapi-ts-client
  ./build-typescript-client.sh
  if [ $? -ne 0 ] ; then
    echo "Failed to build OpenAPI client. Please check the build script."
    exit 1  
  fi
  cd ../ide-plugins/vscode
fi

# This script builds the plugin and prepares it for distribution.
echo "Installing necessary dependencies..."
npm ci
npm run build
if [ $? -ne 0 ] ; then
  echo "Failed to build the web UI. Please check the build script."
  exit 1
fi