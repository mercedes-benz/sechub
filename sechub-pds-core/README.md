<!-- SPDX-License-Identifier: MIT --->

# About this project
## Why a core project?
We got `sechub-test` project which does an overall testing (e.g. packaging levels etc.) and so also a 
testing that the autark PDS application code is in logically sync with origin SecHub parts 
(e.g. PDSScanType and "normal" ScanType)

The problem is, that jar output for a executable spring boot applications cannot be used directly in gradle as 
a project dependency - it's not a normal jar. To avoid writing special code to create different jars (one for
test dependency and one for real starting) this project has been created and will be used as test dependency.

## Content
Core parts must contain at least all parts used in over-all testing.

It shall also be the base for potential subprojects - e.g. a `sechub-pds-cli` project could use it as well.
