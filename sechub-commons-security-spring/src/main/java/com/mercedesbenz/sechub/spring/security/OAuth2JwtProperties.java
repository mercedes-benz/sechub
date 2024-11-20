// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(OAuth2JwtProperties.PREFIX)
public class OAuth2JwtProperties {

    static final String PREFIX = "sechub.security.oauth2.jwt";
    private final Boolean enabled;
    private final String jwkSetUri;

    OAuth2JwtProperties(Boolean enabled, String jwkSetUri) {
        this.enabled = requireNonNull(enabled, "Property '%s.%s' must not be null".formatted(PREFIX, "enabled"));
        this.jwkSetUri = requireNonNull(jwkSetUri, "Property '%s.%s' must not be null".formatted(PREFIX, "jwk-set-uri"));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }
}