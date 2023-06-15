#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

export PATH="$TOOL_FOLDER/pmd/bin:$PATH"

ruleset="ruleset-security.xml"

case "$PMD_RULESET" in
    "SECURITY")
        ruleset="ruleset-security.xml"
    ;;
    "ALL")
        ruleset="ruleset-all.xml"
esac

run.sh pmd --rulesets "$SCRIPT_FOLDER/$ruleset" --format sarif --dir "$PDS_JOB_EXTRACTED_SOURCES_FOLDER" --report-file "$PDS_JOB_RESULT_FILE"

# PMD returns an exit code of 4 in case findings are found
exit 0
