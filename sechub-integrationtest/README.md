<!-- SPDX-License-Identifier: MIT --->
# About sechub-integrationtest project
This project is the final sechub integration test project. 

Using special stuff only by enabling profile `Profiles.INTEGRATIONTEST`

## Concept "use rest api only"
The complete test and setup __is done by API calls only!__
There must be an existing server running to execute the tests! 

## Why the "use rest api only" way?
Doing this will provide following

- database:
 - switch between H2 and also real postgres database implementation without any changes but switch to another profile
 - we delete and create only parts defined in scenarios. So even when a developer accidently would use the PROD system for
    integration testing he/she would never kill the complete database... 
  
- future ready:
   - even works when sechub server is divided into different kubernetes PODs (e.g. seperated administration server).

- real live integration

## Setup
For initial setup an existing super admin ("integrationtestadm") must be created on start which is done by Profiles.INTEGRATIONTEST automatically

## Dependencies
The project may not have any dependency to sechub code parts and MUST use only the official API as any other user.

The only dependencys are to `sechub-testframework`  because of using common utilitity methods for testing and to `sechub-adapter` because we reuse rest trustall implementation.