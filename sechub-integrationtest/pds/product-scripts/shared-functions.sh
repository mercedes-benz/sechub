#!/bin/bash
# SPDX-License-Identifier: MIT

source ./shared/shared-logging.sh
source ./shared/shared-test-variables.sh
source ./shared/shared-merging.sh

# Let us included shared messaging
# It is also used in 
source ./shared/shared-messaging-referenced-in-documentation-as-example.sh
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

