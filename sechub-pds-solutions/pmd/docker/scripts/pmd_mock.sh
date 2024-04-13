#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# Mock is security scan output of CWE506_Embedded_Malicious_Code__aes_encrypted_payload_01.java from https://samate.nist.gov/SARD/downloads/test-suites/2017-10-01-juliet-test-suite-for-java-v1-3.zip

echo "Running PDS PMD Mock"

case "$PMD_RULESET" in
    "SECURITY" | "")
            echo "Scanned with option SECURITY(default)"
            cp "$MOCK_FOLDER/mock-option-security.sarif.json" "$PDS_JOB_RESULT_FILE"
    ;;

    "ALL")
            echo "Scanned with option ALL"
            cp "$MOCK_FOLDER/mock-option-all.sarif.json" "$PDS_JOB_RESULT_FILE"
esac
