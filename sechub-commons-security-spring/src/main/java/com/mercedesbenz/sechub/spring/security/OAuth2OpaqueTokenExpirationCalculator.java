// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.*;

import java.time.Instant;

import org.springframework.stereotype.Component;

@Component
public class OAuth2OpaqueTokenExpirationCalculator {

    public boolean isExpired(OAuth2OpaqueTokenIntrospectionResponse response, Instant now) {
        requireNonNull(response, "parameter 'response' may not be null!");
        requireNonNull(now, "parameter 'now' may not be null!");

        Instant expires = response.getExpiresAt();
        if (expires == null) {
            return true;
        }
        return expires.isBefore(now);
    }
}
