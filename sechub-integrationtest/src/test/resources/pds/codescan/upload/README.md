<!-- SPDX-License-Identifier: MIT --->
# About
Inside this folder are zipfiles used for pds integration testing (direct + integrated with
SecHub).

# Only for testing
At integration test time there is a special importer available being able
to import this. But will not be available at production level.

# Format
The used format is very simple: The zip file contains exact one file: data.txt

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

At the moment thats all for integration testing, so importers will just add some additional informations
if needed. The message and severity are enough for testing.

## Comments
You can use `#` for comments.

Some comments are used as identifiers as well.
E.g. The integration test PDS script for CodeScan will add

- `#PDS_INTTEST_PRODUCT_CODESCAN` to mark this as a pds code scan to import for integration tests
- `#PDS_INTTEST_PRODUCT_CODESCAN_FAILED` to mark this as a failed product execution
