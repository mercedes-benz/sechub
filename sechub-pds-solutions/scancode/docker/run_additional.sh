#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

has_install_problems="no"

# Start scancode to ensure it is installed correctly
echo "Installed product versions:"
scancode --version

if [ "$?" -ne 0 ] ; then
	has_install_problems="yes"
fi

java -jar "$TOOL_FOLDER/tools-java-${SPDX_TOOL_VERSION}-jar-with-dependencies.jar" Version

if [ "$?" -ne 0 ] ; then
	has_install_problems="yes"
fi

if [ "$has_install_problems" = "yes" ] ; then
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  echo "! Problems occured when calling tools. Please fix !"
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
fi
