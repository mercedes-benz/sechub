#!/bin/bash
# SPDX-License-Identifier: MIT

# This script builds the typescript OpenAPI client for the Sechub API (used by sechub-web-ui and ide-plugins/vscode).
set -e
which npm >/dev/null 2>&1 || {
  echo >&2 "npm is not installed. Please install NVM (Node Version Manager) to install node.js."
  exit 1
}

echo "Installing necessary npm packages..."
npm ci
echo "Generating OpenAPI client..."
npm run generate-api-client
echo "Building the OpenAPI client..."
npm run build
if [ $? -ne 0 ]; then
  echo "Error: Build failed. Please check the output for errors."
  exit 1
else
  echo "OpenAPI client built successfully."
fi
