#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

export PATH="$TOOL_FOLDER/pmd/bin:$PATH"
run.sh pmd --rulesets category/java/security.xml,category/jsp/security.xml  --format sarif --dir "$PDS_JOB_SOURCECODE_UNZIPPED_FOLDER" --report-file "$PDS_JOB_RESULT_FILE"

# PMD returns an exit code of 4 in case findings are found
exit 0