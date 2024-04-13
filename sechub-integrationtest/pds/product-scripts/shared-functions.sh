#!/bin/bash
# SPDX-License-Identifier: MIT
set -e
SHARED_FUNCTIONS_DIR=$(dirname -- "$0");
SHARED_DIR="$SHARED_FUNCTIONS_DIR/shared"

source "$SHARED_DIR/shared-constants.sh"
source "$SHARED_DIR/shared-logging.sh"
source "$SHARED_DIR/shared-events.sh"
source "$SHARED_DIR/shared-test-variables.sh"
source "$SHARED_DIR/shared-merging.sh"

source "$SHARED_DIR/shared-messaging-referenced-in-documentation-as-example.sh"
# Usage:
# 
# ----
# infoMessage "this is an info message"
# warnMessage "this is a warning message"
# errorMessage "this is an error message
#     with multiple lines... 
# "
# ----
# 
# The created message file names from the example above look like this: 
# 
# $job_folder_workspace/output/messages
#├── ERROR_message_2022-06-24_17.56.52_822554054.txt
#├── INFO_message_2022-06-24_17.56.52_818872869.txt
#└── WARNING_message_2022-06-24_17.56.52_820825342.txt

