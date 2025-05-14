#!/bin/bash
set -e

cd `dirname $0`
source include.sh

rm -rf "$SECHUB_K8S_BUILDDIR"
mkdir "$SECHUB_K8S_BUILDDIR"
echo "### Preparing k8s configs from templates"
cp -r "$SECHUB_K8S_TEMPLATEDIR"/* "$SECHUB_K8S_BUILDDIR"/
cd "$SECHUB_K8S_BUILDDIR"
find . -type f | sort | while read file ; do
  echo $file
  # Replace template variables with values from env vars
  sed -i "s|{{ *\.SECHUB_NAMESPACE *}}|$SECHUB_NAMESPACE|g" "$file"
  sed -i "s|{{ *\.SECHUB_SERVER_IMAGE_REGISTRY *}}|$SECHUB_SERVER_IMAGE_REGISTRY|g" "$file"
  sed -i "s|{{ *\.SECHUB_SERVER_IMAGE_TAG *}}|$SECHUB_SERVER_IMAGE_TAG|g" "$file"
done
