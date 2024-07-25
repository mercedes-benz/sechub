#!/usr/bin/bash
# SPDX-License-Identifier: MIT

cat "__test__/integrationtest/test-product-output/example-owasp-zap-sarif-output-red.json" > "${PDS_JOB_RESULT_FILE}"
