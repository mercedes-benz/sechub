<!-- SPDX-License-Identifier: MIT --->

# About
This is only a README about the format used for integration test webscan results.

# Is this stuff used in real?
No, its only for testing. At integration test time there is a special importer available being able 
to import this. But will not be available at production level.

# Format
The used format is very simple: Just write text content to result file

## Finding data
Inside this textfile we got different lines containing: 
`${severity}:${message}`

 Examples:

```
 CRITICAL:i am a critical error
 MEDIUM:i am a medium error
 LOW:i am just a low error
 INFO:i am just an information
```


Severity must be one of the provided `SerecoSeverity.java` parts

At the moment thats all for integration testing, so importers will just add some additional stuff
if needed. The message and severity are enough for testing.

## Comments
With a `#` comments are possible.

Some comments are used as identifiers as well.
E.g. Integration test PDS script for WebScna will add

- `#PDS_INTTEST_PRODUCT_WEBSCAN` to mark this as an pds code scon to import for  integration tests
- `#PDS_INTTEST_PRODUCT_WEBSCAN_FAILED` to mark this as an failed product execution
