// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@ConditionalOnBean(SecurityProperties.class)
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

    /**
     * Normally we could inject the {@link SecurityProperties} class in the
     * {@link JwtDecoderConfiguration} class and check if the OAuth2 mode is
     * enabled, but there is no way to make sure that Spring initializes the
     * {@link SecurityProperties} bean before {@link JwtModeEnabledCondition} class.
     * That's why we have to check for the property manually here.
     */
    static class JwtModeEnabledCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, @SuppressWarnings("NullableProblems") AnnotatedTypeMetadata metadata) {
            String mode;
            int count = 0;
            boolean isOAuth2ModeEnabled = false;

            do {
                mode = context.getEnvironment().getProperty(SecurityProperties.Server.PREFIX + ".modes[%d]".formatted(count++));
                if (SecurityProperties.Server.OAUTH2.equals(mode)) {
                    isOAuth2ModeEnabled = true;
                    break;
                }
            } while (mode != null);

            if (!isOAuth2ModeEnabled) {
                return false;
            }

            String oAuth2ModePrefix = SecurityProperties.Server.OAuth2.PREFIX + ".mode";
            String oAuth2ModeProperty = context.getEnvironment().getProperty(oAuth2ModePrefix);
            requireNonNull(oAuth2ModeProperty, "Property %s must not be null".formatted(oAuth2ModePrefix));
            return SecurityProperties.Server.OAuth2.OAUTH2_JWT_MODE.equals(oAuth2ModeProperty);
        }
    }
}
