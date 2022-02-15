#!/bin/bash

# SPDX-License-Identifier: MIT

cd script
FILENAME_GEN_CONST_FP='constants_filepatterns_gen.go'
echo "generate '$FILENAME_GEN_CONST_FP'"
./go-gen-supported-source-extensions.sh > ./../../sechub-cli/src/mercedes-benz.com/sechub/cli/$FILENAME_GEN_CONST_FP