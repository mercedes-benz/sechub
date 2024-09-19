#!/bin/sh
# SPDX-License-Identifier: MIT

if [ "$PDS_JOB_HAS_EXTRACTED_SOURCES" = "true" ] ; then
    echo "Folder structure:"
    echo ""
    tree "$PDS_JOB_EXTRACTED_SOURCES_FOLDER"
else
    echo "No extracted source code found"
    exit 1
fi
