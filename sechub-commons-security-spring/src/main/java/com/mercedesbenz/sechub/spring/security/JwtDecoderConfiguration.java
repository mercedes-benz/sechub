// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@Conditional(JwtDecoderConfiguration.JwtModeEnabledCondition.class)
class JwtDecoderConfiguration {

    @Bean
    JwtDecoder jwtDecoder(SecurityProperties securityProperties) {
        /*
         * @formatter:off
         * The `NimbusJwtDecoder` is a `JwtDecoder` implementation that utilizes the Nimbus JOSE + JWT library to decode JSON Web Tokens (JWTs).
         * It requires a JWK Set URI to fetch the public keys from the Identity Provider (IDP) to verify the JWT's signature.
         */
        return NimbusJwtDecoder
                .withJwkSetUri(securityProperties.getServer().getOAuth2().getJwt().getJwkSetUri())
                .build();
        /* @formatter:on */
    }

    static class JwtModeEnabledCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, @SuppressWarnings("NullableProblems") AnnotatedTypeMetadata metadata) {
            String modePrefix = SecurityProperties.Server.OAuth2.PREFIX + ".mode";
            String property = context.getEnvironment().getProperty(modePrefix);
            return SecurityProperties.Server.OAuth2.OAUTH2_JWT_MODE.equals(requireNonNull(property, "Property %s must not be null".formatted(modePrefix)));
        }
    }
}
