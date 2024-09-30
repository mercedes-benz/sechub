<!-- SPDX-License-Identifier: MIT -->

# SecHub WebUI

## Overview

SecHub WebUI is a web-based user interface for managing and interacting with the SecHub application. This document provides instructions for setting up and running the application locally.

## Profiles

To start the application locally use the `webui_local` profile.

This will include the following profiles:

- `ssl-cert-provided`: a default ssl certificate will be used by the WebUI server
- `basic-auth-mocked`: mock the SecHub Server & enable login with preconfigured credentials at `/login/classic`)
- `local`: includes any local configurations matching `application-local.${USER}.yml`

If you want to provide local configurations, create a file named `application-local.${USER}.yml` in the `src/main/resources` directory.
Make sure that the ${USER} part matches your system username.

This will enable configurations suitable for local development and testing.



