<!-- SPDX-License-Identifier: MIT --->
# About sechub-integrationtest project

This project contains SecHub and PDS integration tests. The tests are done by a special integration 
test framework which can be found inside `src/main/java`. The integration tests are inside `src/test/java`. 

The framework does use some special SecHub and PDS controllers which are only available when the
servers are started with their activated integration test spring profiles 
(SecHub: `integrationtest`, PDS: `pds_integrationtest` ).

## How it works
Testing and setup __are done by API calls only__. 
`gradlew integrationtest` will start a SecHub and a PDS server in integration test mode, execute the 
integration tests and afterwards stop the integration test servers.

### Why the "use rest API only" way?

- future ready: Will even work when SecHub server is no longer a self contained system but
  would be deployed into separate units

- "real live" integration



