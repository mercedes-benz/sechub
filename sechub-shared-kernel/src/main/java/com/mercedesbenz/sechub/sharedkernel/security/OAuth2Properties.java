// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security;

import static java.util.Objects.requireNonNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(OAuth2Properties.PREFIX)
public class OAuth2Properties {

    static final String PREFIX = "sechub.security.oauth2";

    private final String jwkSetUri;

    public OAuth2Properties(String jwkSetUri) {
        this.jwkSetUri = requireNonNull(jwkSetUri, "The property 'sechub.security.oauth2.jwk-set-uri' must not be null");
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }
}