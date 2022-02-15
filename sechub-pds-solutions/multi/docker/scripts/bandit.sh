#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

confidence=""

case "$BANDIT_CONFIDENCE" in
    "HIGH")
        confidence="-iii"
    ;;
    "MEDIUM")
        confidence="-ii"
    ;;
    "LOW")
        confidence="-i"
esac

severity=""

case "$BANDIT_SEVERITY" in
    "HIGH")
        severity="-lll"
    ;;
    "MEDIUM")
        severity="-ll"
    ;;
    "LOW")
        severity="-l"
esac

bandit --format sarif --ignore-nosec $severity $confidence --output "$PDS_JOB_RESULT_FILE" --recursive "$PDS_JOB_SOURCECODE_UNZIPPED_FOLDER/"

exit 0