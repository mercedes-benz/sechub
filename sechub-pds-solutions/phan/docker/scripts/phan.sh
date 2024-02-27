#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

#Insert source code folder into the phan config file
sed -i '5d' /pds/.phan/config.php
sed -i "5 i '$PDS_JOB_SOURCECODE_UNZIPPED_FOLDER'" /pds/.phan/config.php

cd /pds
vendor/bin/phan > "$PDS_JOB_RESULT_FILE"

# Phan returns an exit code of 1 in case of findings
exit 0
