<!-- SPDX-License-Identifier: MIT --->

# About
This is only a README about the formats used for integration test webscan results.

# Different PDS integration test outputs

## File based result
Most PDS test server scripts do just copy existing file content to result output file (e.g. test execution profile 8).
Those copied result files are real security product outputs from manual scans - e.g. OWASP ZAP SARIF result - and are located inside this folder.

## Synthetic text file output
As we also wanted to test in a product independent way, a "synthetic" format was defined. This is provided by `IntegrationTestPDSCWebScanImporter.java` which is a special importer for this kind of format. This importer is only available for integration tests and not in production.

- Inside those textfiles we got different lines containing: 
`${severity}:${message}`

- Severity must be one of the provided `SerecoSeverity.java` parts

- With a `#` comments are possible.

Some comments are used as identifiers as well:
  - `#PDS_INTTEST_PRODUCT_WEBSCAN` to mark this as an pds code scon to import for  integration tests
  - `#PDS_INTTEST_PRODUCT_WEBSCAN_FAILED` to mark this as an failed product execution

Example:

```
 #PDS_INTTEST_PRODUCT_WEBSCAN
 CRITICAL:i am a critical error
 MEDIUM:i am a medium error
 LOW:i am just a low error
 INFO:i am just an information
```
