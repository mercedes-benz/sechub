<!-- SPDX-License-Identifier: MIT --->
# Client test files
in this folder you will find some files like `sechub-prod-codescan1.json` which can be applied to installed sechub client.

## How to use
### Ensure you got an installed sechubclient at your machine
just type `sechub -version` in a `bash`.

You should have now information about sechub client version or an error when not installed.

#### Install local if necessary
If not installed or you want a newer client version just call `./gradlew installGoClientLocal`

### Execute sechub client with script
go to the folder where the scripts are located and execute sechub:
```
SECHUB_APITOKEN=$yourApiToken
sechub -configfile sechub-prod-codescan1.json -user $yourUser scan
```
