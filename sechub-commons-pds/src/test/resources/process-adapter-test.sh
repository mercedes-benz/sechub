#!/bin/bash 
# SPDX-License-Identifier: MIT

#
# Usage: inside a junit test we call this to verify the process adapter implementation works
# as expected
# $1 = filename
# user input = user input...
echo "Starting $0"
read -p "Please enter some user input: " user_input
echo "Writing to file : $1"
echo "user-input=$user_input" > $1