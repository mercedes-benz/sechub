// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;

@Component
public class OAuth2TokenExpirationCalculator {

    public boolean isExpired(OAuth2OpaqueTokenIntrospectionResponse response, Instant now) {
        requireNonNull(response, "parameter 'response' may not be null!");
        requireNonNull(now, "parameter 'now' may not be null!");

        Instant expires = response.getExpiresAtAsInstant();
        if (expires == null) {
            return true;
        }
        return expires.isBefore(now);
    }

    public Instant calculateAccessTokenDuration(Instant now, Duration defaultDuration, OAuth2AccessToken oAuth2AccessToken, Duration minimumTokenValidity) {
        requireNonNull(now, "Parameter 'now' may not be null!");
        requireNonNull(defaultDuration, "Parameter 'defaultDuration' may not be null!");
        requireNonNull(oAuth2AccessToken, "Parameter 'oAuth2AccessToken' may not be null!");
        requireNonNull(minimumTokenValidity, "Parameter 'minimumTokenValidity' may not be null!");

        Instant expiresAt = requireNonNullElseGet(oAuth2AccessToken.getExpiresAt(), () -> now.plusSeconds(defaultDuration.toSeconds()));
        if (minimumTokenValidity != null) {
            Instant minimumTokenValidityInstant = now.plusSeconds(minimumTokenValidity.toSeconds());
            if (minimumTokenValidityInstant.isAfter(expiresAt)) {
                expiresAt = minimumTokenValidityInstant;
            }
        }
        return expiresAt;
    }

}
