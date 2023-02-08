#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# Start scancode to ensure it is installed correctly
scancode --version

if [ "$?" -eq 0 ]
then
  echo "Scancode installed properly."
else
  exit 1
fi