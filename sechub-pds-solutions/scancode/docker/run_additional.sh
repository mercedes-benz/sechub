#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# Start scancode to ensure it is installed correctly
echo "Installed product versions:"
scancode --version

if [ "$?" -ne 0 ] ; then
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  echo "! Problems occured when calling scancode. Please fix !"
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
fi
