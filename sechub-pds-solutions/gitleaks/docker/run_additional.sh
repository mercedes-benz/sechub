#!/bin/bash
# SPDX-License-Identifier: MIT

# Start gitleaks to ensure it is installed correctly
PATH+=":$TOOL_FOLDER/gitleaks"
echo "Installed GitLeaks version:"
gitleaks version

if [ "$?" -ne 0 ] ; then
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  echo "! Problems occured when calling Gitleaks. Please fix !"
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
fi
