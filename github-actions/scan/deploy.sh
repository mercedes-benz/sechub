#!/bin/bash

set -e

echo "--------------------------------"
echo "Deployment of SecHub scan action"
echo "--------------------------------"
echo "[SETUP]"
npm install

echo "[BUILD]"
npm run build

echo "[TEST]"
npm run test

# Check for any changes in the repository
changed_files=$(git diff --name-only HEAD)

# Check if there are no changes
if [ -z "$changed_files" ]; then
  echo "No changes detected in the repository."
  echo "[CANCELED]"

  exit 5
fi

# Check if the only changed file is index.js
if [ "$changed_files" != "github-actions/scan/dist/index.js" ]; then
  echo "Changes detected in files other than index.js (only):"
  echo "$changed_files"
  echo ""
  echo "This may not happen on a deployment! Check the other changes"
  echo "[FAILED]"
  exit 1
fi
echo "Only index.js has changes, deployment is possible."
echo "[DEPLOY]"

git add --all
git commit -m "GitHub action (scan) deployment"
git push



