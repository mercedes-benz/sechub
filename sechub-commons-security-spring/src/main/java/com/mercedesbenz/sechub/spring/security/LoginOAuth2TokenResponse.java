// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the response returned by an OAuth2 or OpenID Connect (OIDC) login
 * authentication flow. This class encapsulates the access token, token type, ID
 * token, expiration time, and refresh token. It is primarily used for handling
 * token-based authentication in secure API communication.
 *
 * <p>
 * The {@code TokenResponse} object is constructed from JSON using Jackson,
 * mapping the expected token fields from the authentication response.
 * </p>
 *
 * <p>
 * Fields:
 * <ul>
 * <li>{@code accessToken}: The access token used to authenticate subsequent
 * requests to the API. Can be either a JWT or opaque token.</li>
 * <li>{@code tokenType}: The type of the token (typically "Bearer").</li>
 * <li>{@code idToken}: The ID token, which contains identity claims about the
 * authenticated user.</li>
 * <li>{@code expiresIn}: The duration in seconds until the access token
 * expires.</li>
 * <li>{@code refreshToken}: The token used to obtain a new access token without
 * re-authenticating (optional).</li>
 * </ul>
 * </p>
 *
 * @author hamidonos
 */
class LoginOAuth2TokenResponse {

    private static final String JSON_PROPERTY_ACCESS_TOKEN = "access_token";
    private static final String JSON_PROPERTY_TOKEN_TYPE = "token_type";
    private static final String JSON_PROPERTY_ID_TOKEN = "id_token";
    private static final String JSON_PROPERTY_EXPIRES_IN = "expires_in";
    private static final String JSON_PROPERTY_REFRESH_TOKEN = "refresh_token";

    private final String accessToken;
    private final String tokenType;
    private final String idToken;
    private final Long expiresIn;
    private final String refreshToken;

    /* @formatter:off */
    @JsonCreator
    LoginOAuth2TokenResponse(@JsonProperty(JSON_PROPERTY_ACCESS_TOKEN) String accessToken,
                             @JsonProperty(JSON_PROPERTY_TOKEN_TYPE) String tokenType,
                             @JsonProperty(JSON_PROPERTY_ID_TOKEN) String idToken,
                             @JsonProperty(JSON_PROPERTY_EXPIRES_IN) Long expiresIn,
                             @JsonProperty(JSON_PROPERTY_REFRESH_TOKEN) String refreshToken) {
        this.accessToken = requireNonNull(accessToken, JSON_PROPERTY_ACCESS_TOKEN + " must not be null");
        this.tokenType = requireNonNull(tokenType, JSON_PROPERTY_TOKEN_TYPE + " must not be null");
        this.idToken = requireNonNull(idToken, JSON_PROPERTY_ID_TOKEN + " must not be null");
        this.expiresIn = requireNonNull(expiresIn, JSON_PROPERTY_EXPIRES_IN + " must not be null");
        this.refreshToken = refreshToken;
    }
    /* @formatter:on */

    String getAccessToken() {
        return accessToken;
    }

    String getTokenType() {
        return tokenType;
    }

    String getIdToken() {
        return idToken;
    }

    Long getExpiresIn() {
        return expiresIn;
    }

    String getRefreshToken() {
        return refreshToken;
    }
}
