#!/bin/bash
# SPDX-License-Identifier: MIT

cd `dirname $0`

# Check if npm is installed
which npm
if [ $? -ne 0 ] ; then
  echo "Website build relies on npm. Please install npm / Node.js"
  exit 1
fi

set -e

echo "# SecHub website: Install dependencies"
npm ci

echo "# SecHub website: Build for production"
npm run build
