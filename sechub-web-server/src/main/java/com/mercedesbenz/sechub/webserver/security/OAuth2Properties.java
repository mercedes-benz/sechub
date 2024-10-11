// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import static java.util.Objects.requireNonNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = OAuth2Properties.PREFIX)
public final class OAuth2Properties {

    static final String PREFIX = "sechub.security.oauth2";
    private static final String ERR_MSG_FORMAT = "The property '%s.%s' must not be null";

    private final String clientId;
    private final String clientSecret;
    private final String provider;
    private final String redirectUri;
    private final String issuerUri;
    private final String authorizationUri;
    private final String tokenUri;
    private final String userInfoUri;
    private final String jwkSetUri;

    /* @formatter:off */
    @ConstructorBinding
    OAuth2Properties(String clientId,
                     String clientSecret,
                     String provider,
                     String redirectUri,
                     String issuerUri,
                     String authorizationUri,
                     String tokenUri,
                     String userInfoUri,
                     String jwkSetUri) {
        this.clientId = requireNonNull(clientId, ERR_MSG_FORMAT.formatted(PREFIX, "client-id"));
        this.clientSecret = requireNonNull(clientSecret, ERR_MSG_FORMAT.formatted(PREFIX, "client-secret"));;
        this.provider = requireNonNull(provider, ERR_MSG_FORMAT.formatted(PREFIX, "provider"));
        this.redirectUri = requireNonNull(redirectUri, ERR_MSG_FORMAT.formatted(PREFIX, "redirect-uri"));
        this.issuerUri = requireNonNull(issuerUri, ERR_MSG_FORMAT.formatted(PREFIX, "issuer-uri"));
        this.authorizationUri = requireNonNull(authorizationUri, ERR_MSG_FORMAT.formatted(PREFIX, "authorization-uri"));
        this.tokenUri = requireNonNull(tokenUri, ERR_MSG_FORMAT.formatted(PREFIX, "token-uri"));
        this.userInfoUri = requireNonNull(userInfoUri, ERR_MSG_FORMAT.formatted(PREFIX, "user-info-uri"));
        this.jwkSetUri = requireNonNull(jwkSetUri, ERR_MSG_FORMAT.formatted(PREFIX, "jwk-set-uri"));
    }
    /* @formatter:on */

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getProvider() {
        return provider;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getIssuerUri() {
        return issuerUri;
    }

    public String getAuthorizationUri() {
        return authorizationUri;
    }

    public String getTokenUri() {
        return tokenUri;
    }

    public String getUserInfoUri() {
        return userInfoUri;
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }
}