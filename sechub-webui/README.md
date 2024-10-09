<!-- SPDX-License-Identifier: MIT -->

# SecHub WebUI

## Overview

SecHub WebUI is a web-based user interface for managing and interacting with the SecHub application.

## Profiles

To start the application locally use the `webui_local` profile.

This will include the following profiles:

- `ssl-cert-provided`: a default ssl certificate will be used by the WebUI server
- `basic-auth-mocked`: mock the SecHub Server & enable login with preconfigured credentials at `/login/classic`)
- `local`: includes any local configurations matching `application-local.${USER}.yml`

If you want to provide local configurations, create a file named `application-local.${USER}.yml` in the `src/main/resources` directory.
Make sure that the ${USER} part matches your system username.

This will enable configurations suitable for local development and testing.

## Running the application in OAuth2 Mode

To run the application in OAuth2 mode, include the `oauth2-enabled` profile.

Note: The `webui_prod` profile includes the `oauth2-enabled` profile.

Make sure that you either provide a valid `application-oauth2-enabled.yml` file in the `src/main/resources` directory or set the required environment variables.

Example `application-oauth2-enabled.yml`:

```yaml
sechub:
  security:
    oauth2:
      client-id: example-client-id
      client-secret: example-client-secret
      provider: example-provider
      redirect-uri: {baseUrl}/login/oauth2/code/{provider}
      issuer-uri: https://sso.provider.example.org
      authorization-uri: https://sso.provider.example.org/as/authorization.oauth2
      token-uri: https://sso.provider.example.org/as/token.oauth2
      user-info-uri: https://sso.provider.example.org/idp/userinfo.openid
      jwk-set-uri: https://sso.provider.example.org/pf/JWKS
```

Alternatively, you can provide the following environment variables:

```bash
SECHUB_SECURITY_OAUTH2_CLIENT_ID=example-client-id
SECHUB_SECURITY_OAUTH2_CLIENT_SECRET=example-client-secret
SECHUB_SECURITY_OAUTH2_PROVIDER=example-provider
SECHUB_SECURITY_OAUTH2_REDIRECT_URI={baseUrl}/login/oauth2/code/{provider}
SECHUB_SECURITY_OAUTH2_ISSUER_URI=https://sso.provider.example.org
SECHUB_SECURITY_OAUTH2_AUTHORIZATION_URI=https://sso.provider.example.org/as/authorization.oauth2
SECHUB_SECURITY_OAUTH2_TOKEN_URI=https://sso.provider.example.org/as/token.oauth2
SECHUB_SECURITY_OAUTH2_USER_INFO_URI=https://sso.provider.example.org/idp/userinfo.openid
SECHUB_SECURITY_OAUTH2_JWK_SET_URI=https://sso.provider.example.org/pf/JWKS
```
