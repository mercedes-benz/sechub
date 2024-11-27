// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@ConditionalOnProperty(value = "sechub.security.oauth2.token-type", havingValue = "JWT")
class JwtDecoderConfiguration {

    @Bean
    JwtDecoder jwtDecoder(OAuth2JwtProperties oAuth2JwtProperties) {
        /*
         * @formatter:off
         * The `NimbusJwtDecoder` is a `JwtDecoder` implementation that utilizes the Nimbus JOSE + JWT library to decode JSON Web Tokens (JWTs).
         * It requires a JWK Set URI to fetch the public keys from the Identity Provider (IDP) to verify the JWT's signature.
         */
        return NimbusJwtDecoder
                .withJwkSetUri(oAuth2JwtProperties.getJwkSetUri())
                .build();
        /* @formatter:on */
    }
}
