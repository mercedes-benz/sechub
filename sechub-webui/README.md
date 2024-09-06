<!-- SPDX-License-Identifier: MIT -->

# SecHub WebUI

## Overview

SecHub WebUI is a web-based user interface for managing and interacting with the SecHub application. This document provides instructions for setting up and running the application locally.

## Profiles

To start the application locally, the following Spring profiles are required:

- `webui_localserver`
- `webui_mocked` (only if you don't want to talk to the actual SecHub server)

These profiles enable configurations suitable for local development and testing.

### `application-local.yaml`

The `application-local.yaml` file must be present in the `src/main/resources` directory. This file contains local configuration settings, including OAuth2 client details.

Example `application-local.yaml`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          oidc-client:
            client-id: your-client-id
            client-secret: your-client-secret
```

**Note:** The `application-local.yaml` file must not be checked into version control to prevent sensitive information from being exposed.
