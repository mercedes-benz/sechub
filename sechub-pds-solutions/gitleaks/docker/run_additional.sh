#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# Start gitleaks to ensure it is installed correctly
echo "Installed GitLeaks version:"
"$TOOL_FOLDER"/gitleaks version

if [ "$?" -ne 0 ] ; then
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  echo "! Problems occured when calling Gitleaks. Please fix !"
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
fi
