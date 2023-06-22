#!/bin/bash
# SPDX-License-Identifier: MIT

function setup_environment_file() {
    local environment_file="$1"
    shift

    if [[ ! -f  "$environment_file" ]]
    then
        echo "Environment file does not exist."
        echo "Creating default environment file $environment_file for you."

        # take arguments from the 2nd to the nth element
        # combine all the files into the settings file
        cat "$@" > "$environment_file"
    else
        echo "Using existing environment file: $environment_file."
    fi
}
