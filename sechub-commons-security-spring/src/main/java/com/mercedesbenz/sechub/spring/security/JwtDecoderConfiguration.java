// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@ConditionalOnExpression("'${sechub.security.server.oauth2.mode}' == 'jwt'")
class JwtDecoderConfiguration {

    private static final String ERR_MSG_FORMAT = "%s must not be null";

    @Bean
    JwtDecoder jwtDecoder(SecHubSecurityProperties secHubSecurityProperties) {
        requireNonNull(secHubSecurityProperties, "SecHubSecurityProperties must not be null");
        SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.JwtProperties jwtProperties = getJwtProperties(secHubSecurityProperties);

        /*
         * @formatter:off
         * The `NimbusJwtDecoder` is a `JwtDecoder` implementation that utilizes the Nimbus JOSE + JWT library to decode JSON Web Tokens (JWTs).
         * It requires a JWK Set URI to fetch the public keys from the Identity Provider (IDP) to verify the JWT's signature.
         */
        return NimbusJwtDecoder
                .withJwkSetUri(jwtProperties.getJwkSetUri())
                .build();
        /* @formatter:on */
    }

    private static SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.JwtProperties getJwtProperties(
            SecHubSecurityProperties secHubSecurityProperties) {
        /* @formatter:off */
        SecHubSecurityProperties.ResourceServerProperties resourceServerProperties = requireNonNull(
                secHubSecurityProperties.getResourceServerProperties(),
                ERR_MSG_FORMAT.formatted(SecHubSecurityProperties.ResourceServerProperties.PREFIX)
        );

        SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties oAuth2Properties = requireNonNull(
                resourceServerProperties.getOAuth2Properties(),
                ERR_MSG_FORMAT.formatted(SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.PREFIX)
        );

        return requireNonNull(
                oAuth2Properties.getJwtProperties(),
                ERR_MSG_FORMAT.formatted(SecHubSecurityProperties.ResourceServerProperties.OAuth2Properties.JwtProperties.PREFIX)
        );
        /* @formatter:on */
    }
}
