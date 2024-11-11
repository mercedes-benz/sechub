// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security;

import static java.util.Objects.requireNonNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(OAuth2Properties.PREFIX)
public class OAuth2Properties {

    static final String PREFIX = "sechub.security.oauth2";
    private static final String ERR_MSG_FORMAT = "The property '%s.%s' must not be null";

    private final String jwkSetUri;

    public OAuth2Properties(String jwkSetUri) {
        this.jwkSetUri = requireNonNull(jwkSetUri, ERR_MSG_FORMAT.formatted(PREFIX, "jwk-set-uri"));
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }
}