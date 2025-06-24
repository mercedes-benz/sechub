#!/bin/bash
# SPDX-License-Identifier: MIT

test_setup_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )

export SECHUB_API_SCRIPT="${test_setup_dir}/../../sechub-developertools/scripts/sechub-api.sh"
export SECHUB_SOLUTION_DIR="${test_setup_dir}/../../sechub-solution/"

if [ "$DEBUG" = "true" ]
then
    echo "SECHUB_API_SCRIPT=${SECHUB_API_SCRIPT}"
    echo "SECHUB_SOLUTION_DIR=${SECHUB_SOLUTION_DIR}"
fi

function initTestEnvironment(){
    echo ""
    echo "[INIT] Test environment"
    
    # We use BASH_SOURCE[1] to get caller script source directory...
    srcdir=$( cd "$( dirname "${BASH_SOURCE[1]}" )" >/dev/null && pwd )

    cd $srcdir 
    echo "Copy environent files - start inspecting directory: $(pwd)"
    
    # Copy the .env file to the current directory
    if [ -e .env ]
    then
        echo "Using existsing .env file"
    else
        echo "Coping .env file from web-ui directory"
        cp ../../.env .
    fi

    # Source the .env file to load environment variables
    set -a
    source ./.env
    set +a

    echo "Using VITE .env to setup your user and apitoken"

    # Export additional variables
    export SECHUB_APITOKEN=${VITE_API_PASSWORD}
    export SECHUB_USERID=${VITE_API_USERNAME}
    export SECHUB_SERVER=https://localhost:8443
    export SECHUB_TRUSTALL=true; 
}
