<!-- SPDX-License-Identifier: MIT --->

# About this project
SecHub PDS stands for sechub product delegation server.

## What does PDS mean?
**In a nutshell:**

A common generic server to easily integrate CLI tools into sechub.

_For details please look into documenation at:_[https://daimler.github.io/sechub/](https://daimler.github.io/sechub/)

## Why is there no dependency to shared kernel or other sechub parts?
PDS is a complete standalone application/server and shall have no dependency to sechub itself.

## Documentation
Documentation can be found in a dedicated main document:

- /sechub-doc/src/docs/asciidoc/sechub-product-delegation-server.adoc

With every release documentation will be updated at [https://daimler.github.io/sechub/](https://daimler.github.io/sechub/)

### Technial information
#### Usescase
Usecases are defined by annotations inside code and generated into documentation (so similar to 
SecHub server documentation).

Please refer `sechub-pds-core/src/main/java/com/daimler/sechub/pds/usecase` and search for references

#### REST documentation
Documentation is done manual here, so differs to sechub server where most parts are generated