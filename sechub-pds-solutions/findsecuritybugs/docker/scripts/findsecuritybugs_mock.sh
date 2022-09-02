#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

# Mock is scan output of: https://github.com/k-tamura/easybuggy
# using the fellowing parameters:
# - "findsecuritybugs.severity": "high"
# - "findsecuritybugs.effort": "max"
# - "findsecuritybugs.include.bugpatterns": "XXE_DOCUMENT,XSS_SERVLET,LDAP_INJECTION,EL_INJECTION,SQL_INJECTION_JDBC,WEAK_MESSAGE_DIGEST_MD5,COMMAND_INJECTION"
echo "Running PDS Find Security Bugs Mock"
cp "$MOCK_FOLDER/find_security_mock.sarif.json" "$PDS_JOB_RESULT_FILE"