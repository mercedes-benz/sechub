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
        return NimbusJwtDecoder.withJwkSetUri(oAuth2JwtProperties.getJwkSetUri()).build();
    }
}
