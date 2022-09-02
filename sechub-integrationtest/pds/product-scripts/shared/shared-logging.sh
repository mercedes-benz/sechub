#!/bin/bash
# SPDX-License-Identifier: MIT

function debug(){
     MESSAGE=$1
     DEBUG=$2
     if [[ "$DEBUG" = "true" ]]; then
        echo "DEBUG:$MESSAGE"
     fi
}
function errEcho () {
    echo "$@" >&2
}
