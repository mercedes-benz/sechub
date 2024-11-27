// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;

import javax.crypto.SealedObject;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

@ConfigurationProperties(OAuth2OpaqueTokenProperties.PREFIX)
public class OAuth2OpaqueTokenProperties {

    static final String PREFIX = "sechub.security.oauth2.opaque-token";
    private static final String ERR_MSG_FORMAT = "Property '%s.%s' must not be null";
    private static final CryptoAccess<String> CRYPTO_STRING = CryptoAccess.CRYPTO_STRING;

    private final String introspectionUri;
    private final SealedObject clientIdSealed;
    private final SealedObject clientSecretSealed;

    /* @formatter:off */
    OAuth2OpaqueTokenProperties(String introspectionUri,
                                String clientId,
                                String clientSecret) {
        this.introspectionUri = requireNonNull(introspectionUri, ERR_MSG_FORMAT.formatted(PREFIX, "introspection-uri"));
        this.clientIdSealed = CRYPTO_STRING.seal(requireNonNull(clientId, ERR_MSG_FORMAT.formatted(PREFIX, "client-id")));
        this.clientSecretSealed = CRYPTO_STRING.seal(requireNonNull(clientSecret, ERR_MSG_FORMAT.formatted(PREFIX, "client-secret")));
    }
    /* @formatter:on */

    public String getIntrospectionUri() {
        return introspectionUri;
    }

    public String getClientId() {
        return CRYPTO_STRING.unseal(clientIdSealed);
    }

    public String getClientSecret() {
        return CRYPTO_STRING.unseal(clientSecretSealed);
    }
}